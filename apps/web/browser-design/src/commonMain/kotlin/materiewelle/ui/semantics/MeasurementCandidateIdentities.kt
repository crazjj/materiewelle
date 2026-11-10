package materiewelle.ui.semantics

/** The voluntary synthetic exercise has no identities in the persisted measurement session. */
enum class MeasurementCandidateId(override val value: String) : ElementId {
    ROOT("measurement-candidate-probe"),
    PLOT("measurement-candidate-plot"),
    CANDIDATE_A("measurement-candidate-a"),
    CANDIDATE_B("measurement-candidate-b"),
    CANDIDATE_C("measurement-candidate-c"),
    KIND_PEAK("measurement-candidate-peak"),
    KIND_FLANK("measurement-candidate-flank"),
    KIND_PLATEAU("measurement-candidate-plateau"),
    NEIGHBORS("measurement-candidate-neighbors"),
    REASON("measurement-candidate-reason"),
    HINT("measurement-candidate-hint"),
    CHECK("measurement-candidate-check"),
    FEEDBACK("measurement-candidate-feedback"),
}
