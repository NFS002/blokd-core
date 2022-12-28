package blokd.config


data class BlokdConfig (
    val nodes: Nodes,
    val validation: Validation,
    val blocks: Blocks,
    val initialState: InitialState,
    val certificateAuthority: CertificateAuthority
)