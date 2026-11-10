"""Reproduzierbare relative Bildauswertung und illustrative DG-Modellvariation.

Unabhängig von den Appfunktionen und dem früheren find_peaks-Skript.
Aufruf: python content/measurement/sensitivitaet.py --output-dir /tmp/materiewelle-measurement
Abhängigkeiten: requirements.txt neben diesem Skript.
Das Originalraster wird nur gelesen. --figures ergänzt optionale PNG/SVG-Diagramme.
Der DG-Fall 42,5 V / 60° ist nur eine Modellvariation, keine historische Messreihe;
der aktive D2-Sekundärbefund im Datenkanon lautet 54 eV / ungefähr 50°.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import math
import os
from pathlib import Path

os.environ.setdefault("MPLCONFIGDIR", "/tmp/materiewelle-matplotlib")
os.environ.setdefault("XDG_CACHE_HOME", "/tmp/materiewelle-cache")
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle
import numpy as np
from PIL import Image

HERE = Path(__file__).resolve().parent
OUT = HERE
ROOT = HERE.parents[1]
SOURCE = ROOT / "content/sources/jonsson-page6-xref33.png"
CANON = ROOT / "content/data/versuchsdaten.json"
IMAGE_HASH = "d8952b3336cb499884f2cbe600cc15645f8bc1c406b1c4e7f43f3bc4d9553bb6"
# Eight disjoint search windows chosen after visual examination of Figure 8.
# Coordinates refer to the entire 703 x 242 raster, not a displayed crop.
WINDOWS = [(545, 558), (558, 572), (572, 586), (586, 600),
           (600, 614), (614, 628), (628, 642), (642, 655)]
BANDS = [("basis", "Breites unteres Band", 100, 220),
         ("oben", "Oberes Band", 20, 45),
         ("unten", "Unteres Band", 145, 220)]
EXTRA_BANDS = [("mitte_oben", "Mittleres oberes Band", 70, 100),
               ("mitte_unten", "Mittleres unteres Band", 100, 145),
               ("fuss", "Schmales unteres Band", 180, 220)]
COLORS = ["#803b50", "#3d6680", "#37796c"]


def write_json(name, data):
    (OUT / name).write_text(json.dumps(data, ensure_ascii=False, indent=2,
                                     allow_nan=False) + "\n")


def spacing_summary(markers):
    positions = np.asarray(markers, dtype=float)
    if positions.shape != (8,) or not np.all(np.isfinite(positions)):
        raise ValueError("Exactly eight finite native x coordinates required")
    gaps = np.diff(positions)
    if not np.all(gaps > 0):
        raise ValueError("Markers must remain in ascending order")
    order = np.arange(8, dtype=float)
    weights = (order - order.mean()) / np.sum((order - order.mean()) ** 2)
    slope = float(weights @ positions)
    mean = float((positions[-1] - positions[0]) / 7)
    return {
        "individual_gaps_px": gaps.tolist(),
        "interval_count": 7,
        "endpoint_mean_spacing_px": mean,
        "sample_gap_sd_px": float(np.std(gaps, ddof=1)),
        "ols_slope_spacing_px": slope,
        "ols_slope_minus_endpoint_px": slope - mean,
    }


def profile_analysis(pixels, band):
    ident, label, y0, y1 = band
    profile = pixels[y0:y1, :].mean(axis=0)
    integer = [lo + int(np.argmax(profile[lo:hi])) for lo, hi in WINDOWS]
    subpixel = []
    for peak, (lo, hi) in zip(integer, WINDOWS):
        # Reject boundary maxima. A clipped search range is not a measured peak.
        if not lo < peak < hi - 1:
            raise ValueError(f"Peak touches search-window edge: {ident} x={peak}")
        left, center, right = profile[peak - 1:peak + 2]
        denominator = left - 2 * center + right
        if denominator >= 0:
            raise ValueError("Local parabolic fit is not concave")
        offset = 0.5 * (left - right) / denominator
        if abs(offset) > .5:
            raise ValueError("Subpixel peak unexpectedly outside nearest pixel")
        subpixel.append(float(peak + offset))
    return {
        "id": ident, "label_de": label,
        "band_y_px": [y0, y1], "band_stop_exclusive": True,
        "marker_y_px": (y0 + y1) / 2,
        "integer_markers_x_px": integer,
        "integer_summary": spacing_summary(integer),
        "parabolic_markers_x_px": subpixel,
        "parabolic_summary": spacing_summary(subpixel),
        "profile_x_start_px": 545,
        "profile_mean_grayscale": profile[545:655].tolist(),
    }


def jonsson(pixels):
    profiles = [profile_analysis(pixels, band) for band in BANDS]
    extras = [profile_analysis(pixels, band) for band in EXTRA_BANDS]
    base = profiles[0]["integer_markers_x_px"]
    base_summary = spacing_summary(base)
    perturbations = []
    for ident, label, changes in [
        ("inner_plus_one", "Innere Marke x₃ um +1 Pixel", {3: 1}),
        ("right_plus_one", "Rechter Endpunkt um +1 Pixel", {7: 1}),
        ("common_plus_one", "Alle Marken um +1 Pixel", dict.fromkeys(range(8), 1)),
        ("endpoints_outward", "Endpunkte jeweils 1 Pixel nach außen", {0: -1, 7: 1}),
    ]:
        moved = [x + changes.get(i, 0) for i, x in enumerate(base)]
        summary = spacing_summary(moved)
        perturbations.append({"id": ident, "label_de": label,
                              "markers_x_px": moved, "summary": summary,
                              "mean_change_px": summary["endpoint_mean_spacing_px"]
                              - base_summary["endpoint_mean_spacing_px"]})

    sigma_x = 1 / math.sqrt(12)  # model: independent Uniform(-.5, .5) rounding
    covariance = np.zeros((7, 7))
    for row in range(7):
        covariance[row, row] = 2 * sigma_x**2
        if row < 6:
            covariance[row, row + 1] = covariance[row + 1, row] = -sigma_x**2
    integer_means = [p["integer_summary"]["endpoint_mean_spacing_px"] for p in profiles]
    all_means = [p[method]["endpoint_mean_spacing_px"] for p in profiles
                 for method in ["integer_summary", "parabolic_summary"]]
    crop_origin = [490, 0]
    # A lossless crop with correct coordinate translation changes no measurements.
    crop = pixels[:, crop_origin[0]:703]
    crop_profile = crop[100:220].mean(axis=0)
    cropped_markers = [lo - 490 + int(np.argmax(crop_profile[lo - 490:hi - 490]))
                       for lo, hi in WINDOWS]
    result = {
        "schema_version": "1.0", "kind": "derived_method_sensitivity_not_new_canonical_data",
        "source_image": str(SOURCE.relative_to(ROOT)), "source_image_sha256": IMAGE_HASH,
        "calculation_script": str(Path(__file__).resolve().relative_to(ROOT)),
        "calculation_script_sha256": hashlib.sha256(Path(__file__).read_bytes()).hexdigest(),
        "source_id": "J1", "pdf_page": 6, "printed_page": 9,
        "figure": "8, right photograph within embedded raster xref33",
        "native_width_px": 703, "native_height_px": 242,
        "coordinate_origin": "upper left of full embedded raster; x right, y down; zero indexed",
        "processing": "Original RGB-to-L grayscale, arithmetic mean over declared y band; no resizing, contrast adjustment or smoothing. One maximum per declared disjoint x window.",
        "search_windows_x_px": [list(w) for w in WINDOWS], "window_stop_exclusive": True,
        "integer_method": "numpy.argmax in each of eight predefined search windows; reject peaks touching a window edge",
        "parabolic_method": "three-point quadratic interpolation about integer peak: offset=0.5*(G[-1]-G[+1])/(G[-1]-2*G[0]+G[+1]); interpolation only, no claim of subpixel accuracy",
        "profiles": profiles, "supplementary_profiles": extras,
        "marker_perturbations": perturbations,
        "crop_control": {"crop_rectangle_xyxy_px": [490, 0, 703, 242],
                         "local_markers_x_px": cropped_markers,
                         "restored_native_markers_x_px": [x + 490 for x in cropped_markers],
                         "mean_spacing_px": spacing_summary(cropped_markers)["endpoint_mean_spacing_px"],
                         "operation": "lossless array crop; no resampling"},
        "method_spreads": {
            "primary_three_integer_min_px": min(integer_means),
            "primary_three_integer_max_px": max(integer_means),
            "primary_three_integer_range_px": max(integer_means) - min(integer_means),
            "primary_three_integer_range_relative_to_base_percent": 100 * (max(integer_means) - min(integer_means)) / integer_means[0],
            "primary_three_bands_two_methods_min_px": min(all_means),
            "primary_three_bands_two_methods_max_px": max(all_means),
            "primary_three_bands_two_methods_range_px": max(all_means) - min(all_means),
            "interpretation": "Observed sensitivity in selected analyses of one raster, not confidence interval, independent replication or historical measurement uncertainty",
        },
        "illustrative_rounding_model": {
            "status": "chosen error model for explanation only; not validated for this photograph",
            "position_error_distribution": "independent uniform rounding in [-0.5,+0.5] native pixel",
            "sigma_position_px": sigma_x,
            "adjacent_gap_correlation": -0.5,
            "gap_covariance_matrix_px2": covariance.tolist(),
            "sigma_endpoint_mean_px": math.sqrt(2) * sigma_x / 7,
            "incorrect_sigma_if_gaps_assumed_independent_px": math.sqrt(2) * sigma_x / math.sqrt(7),
            "worst_case_endpoint_mean_error_px": 1 / 7,
            "ols_sigma_slope_px_under_same_independent_position_model": sigma_x / math.sqrt(42),
            "common_translation_effect_on_spacing": 0,
            "uncertain_scale_effect": "If x'=a*x+b, mean spacing'=a*mean spacing. Translation b cancels; unknown a does not.",
        },
        "absolute_calibration": None,
        "limits": ["Gray level is not calibrated electron intensity", "Single reproduction, no independent acquisitions",
                   "Profiles partly overlap; method spread is not an uncertainty budget",
                   "No absolute wavelength, no derived photograph magnification, no historical uncertainty inferred",
                   "Pixel pitch is not the sole peak-location limit: grain, print scan and fringe geometry also matter"],
        "measurement_performed_by": "AI agent; pupil's own marker placement and interpretation remain open",
    }
    write_json("joensson_sensitivitaet.json", result)
    return result


def dg():
    canon = json.loads(CANON.read_text())
    constants = canon["constants"]
    h, e, m = (constants[key]["value"] for key in ["h", "e", "electron_mass"])
    U0, phi0, D0 = 42.5, 60.0, .215e-9

    def wavelength(U):
        if U <= 0:
            raise ValueError("Accelerating voltage must be positive")
        return h / math.sqrt(2 * m * e * U)

    def ratio(U, phi, D):
        return D * math.sin(math.radians(phi)) / wavelength(U)

    R0 = ratio(U0, phi0, D0)
    cases = []
    for ident, delta_u, delta_phi, delta_d in [
        ("U_minus", -.5, 0, 0), ("U_plus", .5, 0, 0),
        ("phi_minus", 0, -1, 0), ("phi_plus", 0, 1, 0),
        ("D_minus", 0, 0, -.001e-9), ("D_plus", 0, 0, .001e-9),
        ("all_minus", -.5, -1, -.001e-9), ("all_plus", .5, 1, .001e-9),
    ]:
        U, phi, D = U0 + delta_u, phi0 + delta_phi, D0 + delta_d
        rel_linear = delta_d / D0 + math.radians(delta_phi) / math.tan(math.radians(phi0)) + delta_u / (2 * U0)
        exact = ratio(U, phi, D)
        linear = R0 * (1 + rel_linear)
        cases.append({"id": ident, "U_V": U, "phi_deg": phi, "D_nm": D * 1e9,
                      "lambda_NR_nm": wavelength(U) * 1e9,
                      "lambda_surface_nm": D * math.sin(math.radians(phi)) * 1e9,
                      "ratio_exact": exact, "ratio_linear": linear,
                      "signed_residual_exact_percent": 100 * (exact - 1),
                      "signed_residual_linear_percent": 100 * (linear - 1),
                      "linear_minus_exact_percentage_points": 100 * (linear - exact)})
    threshold = h**2 / (2 * m * e * D0**2)
    inversions = []
    for U in [32.0, threshold, 33.0, 42.5]:
        q = wavelength(U) / D0
        # Only the analytically constructed threshold can be clamped for binary rounding.
        valid = q <= 1 or U == threshold
        angle = math.degrees(math.asin(min(q, 1))) if valid else None
        inversions.append({"U_V": U, "lambda_NR_nm": wavelength(U) * 1e9,
                           "lambda_over_D": q, "has_real_angle": valid,
                           "phi_predicted_deg": angle,
                           "dphi_dU_deg_per_V": -math.degrees(math.tan(math.radians(angle))) / (2 * U)
                           if valid and U > threshold else None})
    result = {
        "schema_version": "1.0", "kind": "chosen_model_variation_not_historical_uncertainty",
        "canonical_input_file": str(CANON.relative_to(ROOT)),
        "canonical_input_sha256_at_calculation": hashlib.sha256(CANON.read_bytes()).hexdigest(),
        "source_status": "Illustrative model case 42.5 V / 60 deg retained from a rejected private project plan, not historical evidence. Active canonical D2 case is 54 eV / approximately 50 deg. D=.215nm is a chosen structural input, not fitted from lambda. See content/sources/README.md.",
        "constants": {key: constants[key] for key in ["h", "e", "electron_mass"]},
        "base": {"U_V": U0, "phi_deg": phi0, "D_nm": D0 * 1e9, "order": 1,
                 "lambda_NR_nm": wavelength(U0) * 1e9,
                 "lambda_surface_nm": D0 * math.sin(math.radians(phi0)) * 1e9,
                 "ratio": R0, "signed_residual_percent": 100 * (R0 - 1)},
        "model": "Normal incidence; n=1; phi from outward surface normal; D is row spacing within surface, not Bragg d; nonrelativistic electron momentum",
        "linearization": "R=lambda_surface/lambda_NR; delta R/R0 = delta D/D0 + cot(phi0)*delta phi_rad + delta U/(2*U0)",
        "chosen_variations": {"U_V": .5, "phi_deg": 1, "D_nm": .001,
                              "status": "illustrative parameter ranges, not measured errors or fitted corrections"},
        "cases": cases,
        "inverse_model": {"minimum_voltage_V": threshold,
                          "condition": "lambda(U)/D <=1, equivalently U >= h^2/(2*m*e*D^2), for n=1",
                          "cases": inversions,
                          "derivative": "dphi/dU = -tan(phi)/(2U) in radians per volt; diverges at threshold from above",
                          "interpretation": "Small wavelength residual at32V does not create a real predicted angle in this strict model"},
        "historical_measurement_uncertainty": None,
    }
    write_json("dg_sensitivitaet.json", result)
    return result, ratio


def checks(j, d, ratio):
    """Meaningful analytic invariants; no expectation imported from a prototype."""
    records = []

    def check(name, condition):
        if not condition:
            raise AssertionError(name)
        records.append({"check": name, "status": "passed"})

    close = lambda a, b: math.isclose(a, b, rel_tol=1e-12, abs_tol=1e-12)
    base = j["profiles"][0]
    gaps = base["integer_summary"]["individual_gaps_px"]
    check("Eight selected maxima reproduce direct 96px endpoint span / seven intervals",
          base["integer_markers_x_px"][0] == 551 and base["integer_markers_x_px"][-1] == 647
          and close(sum(gaps) / 7, 96 / 7))
    perturb = {p["id"]: p for p in j["marker_perturbations"]}
    check("Interior-marker motion cancels in endpoint mean but changes adjacent gaps",
          close(perturb["inner_plus_one"]["mean_change_px"], 0)
          and perturb["inner_plus_one"]["summary"]["individual_gaps_px"][2:4] == [15, 13])
    check("One endpoint pixel changes mean by exactly 1/7px",
          close(perturb["right_plus_one"]["mean_change_px"], 1 / 7))
    check("Common translation cancels and lossless crop restores all native coordinates",
          close(perturb["common_plus_one"]["mean_change_px"], 0)
          and j["crop_control"]["restored_native_markers_x_px"] == base["integer_markers_x_px"])
    rounding = j["illustrative_rounding_model"]
    covariance = np.asarray(rounding["gap_covariance_matrix_px2"])
    check("Correlated-gap variance equals independent-endpoint variance, not independent-gap variance",
          close(float(covariance.sum() / 49), rounding["sigma_endpoint_mean_px"] ** 2)
          and min(np.linalg.eigvalsh(covariance)) > 0)
    b = d["base"]
    eps = 1e-4
    numeric = (ratio(b["U_V"], b["phi_deg"] + eps, b["D_nm"] * 1e-9)
               - ratio(b["U_V"], b["phi_deg"] - eps, b["D_nm"] * 1e-9)) / (2 * eps)
    analytic = b["ratio"] / math.tan(math.radians(b["phi_deg"])) * math.pi / 180
    check("Angle derivative per degree independently agrees with central difference",
          math.isclose(numeric, analytic, rel_tol=1e-8))
    check("U scaling and D linearity obey exact ratio identities",
          close(ratio(4 * b["U_V"], b["phi_deg"], b["D_nm"] * 1e-9), 2 * b["ratio"])
          and close(ratio(b["U_V"], b["phi_deg"], 2 * b["D_nm"] * 1e-9), 2 * b["ratio"]))
    inv = d["inverse_model"]["cases"]
    check("32V has no real inverse; analytic threshold gives90degrees;33V is valid",
          not inv[0]["has_real_angle"] and inv[0]["phi_predicted_deg"] is None
          and close(inv[1]["phi_predicted_deg"], 90) and inv[2]["has_real_angle"])
    write_json("pruefprotokoll.json", {"checks": records, "passed": len(records),
                                      "limitation": "Author's analytic self-checks, not an independent reviewer"})


def figures(pixels, j, d, ratio):
    plt.rcParams.update({"font.family": "DejaVu Sans", "font.size": 10,
                         "axes.spines.top": False, "axes.spines.right": False,
                         "svg.hashsalt": "materiewelle-sensitivitaet-v1"})
    fig = plt.figure(figsize=(11, 8.5))
    grid = fig.add_gridspec(4, 2, width_ratios=[1, 1.75], hspace=.58, wspace=.36)
    photo = fig.add_subplot(grid[:3, 0])
    photo.imshow(pixels, cmap="gray", vmin=0, vmax=255)
    photo.set_xlim(490, 703); photo.set_ylim(242, 0)
    for i, profile in enumerate(j["profiles"]):
        low, high = profile["band_y_px"]
        photo.add_patch(Rectangle((542, low), 116, high - low,
                                 fill=False, edgecolor=COLORS[i], linewidth=2))
        ax = fig.add_subplot(grid[i, 1])
        x = np.arange(545, 655)
        values = profile["profile_mean_grayscale"]
        peaks = profile["integer_markers_x_px"]
        ax.plot(x, values, color=COLORS[i], lw=1.6)
        ax.scatter(peaks, np.asarray(values)[np.asarray(peaks) - 545], color=COLORS[i], s=18)
        mean = profile["integer_summary"]["endpoint_mean_spacing_px"]
        ax.set_title(f'{profile["label_de"]}: y ∈ [{low}, {high}) → {mean:.3f} px', loc="left", fontsize=11)
        ax.set_ylabel("Grauwert"); ax.set_xlabel("x / native Pixel"); ax.grid(alpha=.15)
        ax.set_xlim(545, 655)
    photo.set_xlabel("x / native Pixel"); photo.set_ylabel("y / native Pixel")
    photo.set_title("Unveränderter Bildausschnitt\nmit drei Auswertebändern", loc="left", fontsize=11)
    note = fig.add_subplot(grid[3, :]); note.axis("off")
    note.text(0, 1, "Drei Bänder, dasselbe Originalraster", fontweight="bold", fontsize=13, va="top")
    note.text(0, .65, "Ganzzahlige Marken: 13,714–13,857 px. Die Differenz 0,143 px ist Methodenempfindlichkeit.\n"
              "Die Profile sind keine unabhängigen Aufnahmen. Grauwert ist keine kalibrierte Elektronenintensität.\n"
              "Quelle: Jönsson, AJP 42 (1974), Druckseite 9, Abb. 8; eigene relative Auswertung.", va="top", linespacing=1.6)
    fig.suptitle("Jönsson: Was bleibt bei anderer Auswertung gleich?", x=.08, ha="left", fontsize=17, y=.99)
    fig.savefig(OUT / "joensson_profile.png", dpi=150, bbox_inches="tight")
    fig.savefig(OUT / "joensson_profile.svg", bbox_inches="tight", metadata={"Date": None})
    plt.close(fig)

    fig, axes = plt.subplots(1, 3, figsize=(12, 4.8))
    base = d["base"]
    R0, U0, phi0, D0 = base["ratio"], base["U_V"], base["phi_deg"], base["D_nm"]
    configs = [("U / V", np.linspace(42, 43, 101), U0,
                lambda x: ratio(x, phi0, D0 * 1e-9), lambda x: R0 * (1 + (x - U0) / (2 * U0))),
               ("φ / °", np.linspace(59, 61, 101), phi0,
                lambda x: ratio(U0, x, D0 * 1e-9), lambda x: R0 * (1 + math.radians(x - phi0) / math.tan(math.radians(phi0)))),
               ("D / nm", np.linspace(.214, .216, 101), D0,
                lambda x: ratio(U0, phi0, x * 1e-9), lambda x: R0 * (1 + (x - D0) / D0))]
    for ax, (label, values, middle, exact, linear) in zip(axes, configs):
        ax.plot(values, [100 * (exact(x) - 1) for x in values], color=COLORS[0], label="exakt im Modell", lw=2)
        ax.plot(values, [100 * (linear(x) - 1) for x in values], color=COLORS[1], label="linearisiert", ls="--")
        ax.scatter([middle], [100 * (R0 - 1)], color="#222222", s=20, zorder=5)
        ax.axhline(0, color="gray", lw=.8)
        ax.set_ylim(-2.15, .15)
        ax.set_xlabel(label); ax.grid(alpha=.15); ax.ticklabel_format(useOffset=False, axis="x")
    axes[0].set_ylabel("r = (λ Oberfläche / λ nr − 1) · 100 %")
    axes[0].legend(fontsize=8)
    fig.suptitle("DG: ein Parameter variiert, zwei bleiben fest", fontsize=15)
    fig.text(.08, .02, "Basis: 42,5 V · 60° · D = 0,215 nm. Gewählte Modellvariation, keine historischen Messunsicherheiten.", fontsize=9)
    fig.tight_layout(rect=(0, .07, 1, .94))
    fig.savefig(OUT / "dg_variation.png", dpi=150)
    fig.savefig(OUT / "dg_variation.svg", metadata={"Date": None})
    plt.close(fig)


def main():
    global OUT
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-dir", type=Path, default=HERE,
                        help="Directory for generated JSON files (default: script directory)")
    parser.add_argument("--figures", action="store_true",
                        help="Also generate PNG and SVG method illustrations")
    args = parser.parse_args()
    OUT = args.output_dir.resolve()
    if hashlib.sha256(SOURCE.read_bytes()).hexdigest() != IMAGE_HASH:
        raise ValueError("Original raster hash differs; inspect source before recalculation")
    with Image.open(SOURCE) as image:
        if image.size != (703, 242):
            raise ValueError("Unexpected original raster dimensions")
        pixels = np.asarray(image.convert("L"))
    OUT.mkdir(parents=True, exist_ok=True)
    j = jonsson(pixels)
    d, ratio = dg()
    checks(j, d, ratio)
    if args.figures:
        figures(pixels, j, d, ratio)
    print(json.dumps({"jonsson_integer_means_px": [p["integer_summary"]["endpoint_mean_spacing_px"] for p in j["profiles"]],
                      "dg_base_residual_percent": d["base"]["signed_residual_percent"],
                      "self_checks_passed": 8}, ensure_ascii=False))


if __name__ == "__main__":
    main()
