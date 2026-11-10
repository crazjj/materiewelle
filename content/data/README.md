# Fachlicher Datenkanon

[versuchsdaten.json](versuchsdaten.json), Schema 2.0, hält Konstanten, deklarierte
Modelle, Quellenstatus und die dokumentierte relative Jönsson-Bildmessung zusammen.
Die Website und das Physikmodul übernehmen diese Daten beim Build. Es gibt keine
zweite von Hand gepflegte Tabelle im Anwendungscode.

Davisson–Germer verwendet den D2-Sekundärbefund 54 eV / ungefähr 50° und ein
unabhängiges Ni(111)-Strukturmodell. Jönssons nominale Artikelparameter und die
relative Pixelmessung bleiben getrennt. Historische Unsicherheiten und die
absolute Bildkalibrierung sind unbekannt; `null` bedeutet keine Nullunsicherheit.

[Quellen und Herkunft](../sources/README.md) erläutert den übernommenen Prüfstand,
die offenen Quellenfragen und die ungeklärte öffentliche Bildnutzungsgrundlage.
Historische private Dateipfade sind ausdrücklich als Archivbezüge markiert.
[Methodenprüfung](../measurement/README.md) dokumentiert die reproduzierbaren
Profilvarianten, ihre Grenzen und den erzeugten Datenvertrag.
