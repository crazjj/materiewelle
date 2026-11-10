#!/usr/bin/env python3
"""Regression checks for the notation boundary, rather than the typesetter's implementation."""
import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('math_text', Path(__file__).with_name('build-math-text.py'))
module = importlib.util.module_from_spec(spec);spec.loader.exec_module(module)

class NotationTest(unittest.TestCase):
    def test_geometry_points_and_segments_are_typeset_in_controls_and_diagrams(self):
        for point in ('P', 'Q', 'R'):
            self.assertIn(point, list(module.spans('Punkt ' + point)))
            self.assertIn(point, module.collect()[0])
        for segment in ('AP', 'AQ', 'PB', 'BR', 'AB', 'AR'):
            self.assertIn(segment, list(module.spans('Strecke ' + segment)))
            self.assertEqual(' '.join(segment), module.notation(segment))

    def test_new_geometry_letters_do_not_split_class_or_source_identifiers(self):
        self.assertEqual([], list(module.spans('Klasse Q2, Beleg R01 und P12.')))
        self.assertEqual([], list(module.spans('Punktnamen und Querabstände.')))

    def test_ascii_indices_in_authored_tutor_text_are_real_math_not_prose(self):
        self.assertIn('E_kin = e U', list(module.spans('Modell: E_kin = e U.')))
        self.assertIn('m_e', list(module.spans('Die Elektronenmasse m_e ist gegeben.')))
        self.assertEqual(module.notation('Eₖᵢₙ = e U'), module.notation('E_kin = e U'))
        self.assertEqual(module.notation('mₑ'), module.notation('m_e'))
        self.assertIn('E_kin', module.collect()[0])

    def test_initial_kinetic_energy_and_square_root_keep_the_whole_quantity(self):
        energy = 'E_kin = E_kin,0 + eU'
        self.assertIn(energy, list(module.spans('Anfangsenergie: ' + energy + '.')))
        self.assertIn('E_("kin",0)', module.notation(energy))
        self.assertEqual('sqrt(E_"kin")', module.notation('√E_kin'))
        self.assertEqual('sqrt(E_("kin",0))', module.notation('√E_kin,0'))
        ratio = 'E_kin,2/E_kin,1'
        self.assertIn(ratio, list(module.spans(ratio)))
        self.assertIn('E_("kin",2)', module.notation(ratio))
        self.assertNotIn('E_kin,0', list(module.spans('E_kin,01 ist kein freigegebenes Symbol.')))

    def test_angstrom_source_quantity_uses_an_upright_typst_unit(self):
        self.assertIn('3,52 Å', list(module.spans('Gitterkonstante: 3,52 Å; Quelle D2.')))
        self.assertEqual('upright("Å")', module.notation('unit:Å'))
        self.assertIn('upright("Å")', module.notation('3,52 Å'))

    def test_gray_value_symbol_survives_dynamic_numeric_boundaries(self):
        self.assertIn('G = 128', list(module.spans('Am Messcursor gilt G = 128.')))
        pieces=module.kotlin_strings('Text("Grauwert G ${number(value, 2)}")')
        self.assertTrue(any('G' in list(module.spans(part)) for part in pieces))

    def test_historical_year_ranges_are_not_reinterpreted_as_fractions(self):
        self.assertEqual('upright("1923/24")', module.notation('1923/24'))
        self.assertEqual('upright("1923/1924")', module.notation('1923/1924'))
        self.assertNotIn('upright', module.notation('3/8'))

    def test_preserves_complete_fraction_and_subscript_identifiers(self):
        self.assertIn('K = p²/(2mₑ)', list(module.spans('Bei K = p²/(2mₑ) gilt die Näherung.')))
        self.assertIn('p_gamma', module.notation('pγ = h/λ'))
        self.assertIn('x_n', module.notation('xₙ − x₁'))

    def test_source_ids_and_dates_do_not_become_physical_variables(self):
        self.assertEqual([], list(module.spans('Quellen A1, B1, D2 und T01.')))
        self.assertEqual('upright("2026-10-16")', module.notation('2026-10-16'))
        self.assertEqual('upright("150-V")', module.notation('150-V'))
        self.assertEqual('upright("54-eV-/50°")', module.notation('54-eV-/50°'))

    def test_mass_inside_fraction_is_not_the_metre_unit(self):
        self.assertNotIn('upright("m")', module.notation('p²/(2m)'))
        self.assertIn('upright("m")', module.notation('10 m'))
        self.assertEqual('upright("m")', module.notation('unit:m'))
        self.assertIn('upright("m")', module.notation('10⁻¹⁰ m'))
        for quantity in ['2µm', '50pm', '3kg']:
            self.assertIn(quantity,list(module.spans(quantity)))

    def test_scientific_notation_and_decimal_comma_have_no_tex_braces(self):
        converted = module.notation('1,00e-10 m')
        self.assertIn('10^(-10)', converted)
        self.assertNotIn('{,}', converted)
        self.assertNotIn('asqrt', module.notation('a√(3/8)'))
        self.assertNotIn('sqrt[', module.notation('λrel = h / √[2mₑeU (1 + eU/(2mₑc²))]'))

    def test_kotlin_interpolation_is_not_compiled_as_typst_code(self):
        source = 'Text("λ ≈ ${number(value, 4)} pm; ${if (flag) "A" else "B"}")'
        pieces = module.kotlin_strings(source)
        self.assertIn('λ ≈  ¤  pm;  ¤ ', pieces)
        self.assertIn('A', pieces);self.assertIn('B', pieces)
        self.assertNotIn('number(value, 4)', ''.join(pieces))

    def test_function_arguments_and_full_denominators_keep_the_physics(self):
        self.assertIn('sin(theta)', module.notation('π b sin θ / λ'))
        self.assertIn('sin(60 degree)', module.notation('sin 60°'))
        self.assertIn('/ (I(0))', module.notation('I / I(0)'))
        self.assertIn('upright("J s")', module.notation('6,62607015 × 10⁻³⁴ J s'))

    def test_displacements_are_whole_fraction_operands_without_standalone_brackets(self):
        compact = lambda source: ''.join(module.notation(source).split())
        for axis in ('s', 'x', 'y'):
            self.assertEqual('(Delta' + axis + ')/lambda', compact('Δ' + axis + '/λ'))
            self.assertEqual('lambda/(Delta' + axis + ')', compact('λ/Δ' + axis))
            self.assertEqual('Delta' + axis, compact('Δ' + axis))
        self.assertEqual('phi=2pi(Deltas)/lambda', compact('φ = 2πΔs/λ'))
        self.assertEqual('(Deltax(B))/(Deltax(A))', compact('Δx(B)/Δx(A)'))
        self.assertEqual('(Deltas)/(Deltax)', compact('Δs / Δx'))
        self.assertEqual('(Deltas)/lambda', compact('(Δs)/λ'))

    def test_adjacent_factors_and_indices_are_whole_expressions(self):
        for text in ['K = ½mₑv²', 'φ = 2πΔs/λ', 'p = √(2mₑeU)', 'xᵢ₊₁ − xᵢ', 'N − 1', 'B / A', 'A=B']:
            self.assertIn(text, list(module.spans(text)))

if __name__ == '__main__':unittest.main()
