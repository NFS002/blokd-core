package blokd.config

import blokd.extensions.PRIMARY_KEYPAIR
import blokd.extensions.id

object Keys {

    val publicId: String = PRIMARY_KEYPAIR.public.id()
    val privateId: String = PRIMARY_KEYPAIR.private.id()
    val algorithm: String = PRIMARY_KEYPAIR.public.algorithm
}