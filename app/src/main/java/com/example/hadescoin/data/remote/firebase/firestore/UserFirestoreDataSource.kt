package com.example.hadescoin.data.remote.firebase.firestore

class UserFirestoreDataSource(
    private val firestore: Any
) {
    fun getUserByPhoneNumber(phoneNumber: String): FirestoreUserRecord? {
        val snapshot = awaitTask(
            invokeMethod(document(collection(), phoneNumber), "get")
        )
        if (!(snapshot.javaClass.getMethod("exists").invoke(snapshot) as Boolean)) return null

        return FirestoreUserRecord(
            phoneNumber = invokeString(snapshot, PHONE_NUMBER_FIELD) ?: phoneNumber,
            documentNumber = invokeString(snapshot, DOCUMENT_NUMBER_FIELD),
            pin = invokeString(snapshot, PIN_FIELD) ?: ""
        )
    }

    fun registerUser(
        phoneNumber: String,
        documentNumber: String?,
        pin: String
    ) {
        val userData = hashMapOf(
            PHONE_NUMBER_FIELD to phoneNumber,
            DOCUMENT_NUMBER_FIELD to documentNumber,
            PIN_FIELD to pin,
            CREATED_AT_FIELD to System.currentTimeMillis()
        )

        // TODO(security): En produccion NO guardar el PIN en texto plano; usar hash + salt y controles de acceso.
        awaitTask(invokeMethod(document(collection(), phoneNumber), "set", userData))
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val PHONE_NUMBER_FIELD = "phoneNumber"
        private const val DOCUMENT_NUMBER_FIELD = "documentNumber"
        private const val PIN_FIELD = "pin"
        private const val CREATED_AT_FIELD = "createdAt"
    }

    private fun collection(): Any {
        return firestore.javaClass.getMethod("collection", String::class.java)
            .invoke(firestore, USERS_COLLECTION)!!
    }

    private fun document(collection: Any, documentId: String): Any {
        return collection.javaClass.getMethod("document", String::class.java)
            .invoke(collection, documentId)!!
    }

    private fun awaitTask(task: Any): Any {
        val tasksClass = Class.forName("com.google.android.gms.tasks.Tasks")
        val awaitMethod = tasksClass.methods.first { method ->
            method.name == "await" && method.parameterTypes.size == 1
        }
        return awaitMethod.invoke(null, task)!!
    }

    private fun invokeMethod(target: Any, methodName: String, vararg args: Any): Any {
        val method = target.javaClass.methods.first { candidate ->
            candidate.name == methodName && candidate.parameterTypes.size == args.size
        }
        return method.invoke(target, *args)!!
    }

    private fun invokeString(target: Any, fieldName: String): String? {
        return target.javaClass.getMethod("getString", String::class.java)
            .invoke(target, fieldName) as String?
    }
}

data class FirestoreUserRecord(
    val phoneNumber: String,
    val documentNumber: String?,
    val pin: String
)

