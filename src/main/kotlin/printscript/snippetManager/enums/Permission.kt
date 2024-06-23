package printscript.snippetManager.enums

enum class Permission(val permission: String) {
    OWNER("OWNER"),
    SHARED("SHARED"),
    ;

    companion object {
        fun fromString(str: String): Permission {
            for (permission in entries) {
                if (permission.permission == str) {
                    return permission
                }
            }
            throw Exception("Permiso no encontrado")
        }
    }
}
