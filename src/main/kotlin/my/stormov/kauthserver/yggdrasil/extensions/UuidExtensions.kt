package my.stormov.kauthserver.yggdrasil.extensions

import java.util.UUID

val UUID.noDash
    get() = toString().replace("-", "")