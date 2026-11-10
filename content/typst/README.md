# Typst-Ausgaben

Dieser Ordner enthält das dreiseitige [Handout](HANDOUT.md) und die
[Formeln für die Weboberfläche](math/README.md). Die Ausgaben werden beim Build
erzeugt. PDF, SVG und Vorschaubilder gehören in ein separates Ausgabeverzeichnis.

- [qr/website.typ](qr/website.typ): separates A4-Druckblatt mit großem QR-Code zur Website; [PDF ansehen](../../docs/website-qr-a4.pdf).
- [handout.typ](handout.typ): Handout v1.7 mit Herleitung, Davisson–Germer und Jönsson.
- [fachwerte.typ](fachwerte.typ): Berechnungen aus den gemeinsamen
  [Versuchsdaten](../data/versuchsdaten.json), Schema 2.0.
- [vorlage.typ](vorlage.typ) und [auswahl.json](auswahl.json): Satzvorlage und Farben.
- [handoutassets](handoutassets): eigene Vektorgrafik für die Bragg-Konstruktion.
- [math/formulas.json](math/formulas.json): kontrollierter Formelkatalog.
- [requirements.txt](requirements.txt): gepinnte Typst-Bindung.

Vom Repository-Stamm aus:

```sh
python3 content/typst/build-handout.py --output-dir /tmp/materiewelle-handout
python3 content/typst/math/build.py --output-dir /tmp/materiewelle-math
```

Die Buildskripte verwenden die eingecheckten Latin-Modern-Schriften unter
`apps/web/site/src/jsMain/resources/public/fonts`. Das Handout benötigt zusätzlich
DejaVu Sans sowie Poppler zur Prüfung des erzeugten PDFs. Toolchain, Tests und
Modellgrenzen sind in den beiden Dokumentationen beschrieben.

Das QR-Druckblatt lässt sich mit derselben Typst-Bindung und DejaVu Sans bauen:

```sh
python3 -c 'import typst; typst.compile("content/typst/qr/website.typ", output="docs/website-qr-a4.pdf", root="content/typst/qr")'
```

Der Vektorcode in `qr/website.svg` enthält `https://materiewelle.vercel.app/`.
Bei einem Adresswechsel müssen Code und gedruckte Adresse gemeinsam aktualisiert
werden. Der aktuelle Code wurde aus der gerenderten A4-PDF unabhängig zurückgelesen.
