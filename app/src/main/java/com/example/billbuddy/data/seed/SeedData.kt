package com.example.billbuddy.data.seed

import android.util.Log
import com.example.billbuddy.data.model.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

object SeedData {
    private const val TAG = "SeedData"
    private const val SAMPLE_UID = "sample_user_id"

    fun seedSampleData(force: Boolean = false) {
        val firestore = FirebaseFirestore.getInstance()
        Log.d(TAG, "=== BẮT ĐẦU SEED DATA (force=$force) ===")

        // 1. Khởi tạo tài liệu User mẫu trong Firestore
        val usersRef = firestore.collection("users").document(SAMPLE_UID)
        usersRef.get()
            .addOnSuccessListener { doc ->
                Log.d(TAG, "[User] GET thành công, exists=${doc.exists()}")
                if (!doc.exists() || force) {
                    val userModel = User(
                        documentId = SAMPLE_UID,
                        email = "sample@billbuddy.com",
                        displayName = "Người dùng Mẫu",
                        createdAt = Timestamp.now(),
                        updatedAt = Timestamp.now()
                    )
                    usersRef.set(userModel)
                        .addOnSuccessListener { Log.d(TAG, "[User] SET thành công - Đã khởi tạo thông tin người dùng mẫu") }
                        .addOnFailureListener { e -> Log.e(TAG, "[User] SET THẤT BẠI: ${e.message}", e) }
                }
            }
            .addOnFailureListener { e -> Log.e(TAG, "[User] GET THẤT BẠI: ${e.message}", e) }

        // 2. Khởi tạo Danh mục và các dữ liệu liên quan (Chi tiêu mẫu)
        val categoriesRef = firestore.collection("categories")
        categoriesRef.whereEqualTo("userId", SAMPLE_UID).get()
            .addOnSuccessListener { snap ->
                Log.d(TAG, "[Category] QUERY thành công, isEmpty=${snap.isEmpty}, size=${snap.size()}")
                if (snap.isEmpty || force) {
                    val defaultCategories = listOf(
                        Category(name = "Ăn uống", icon = "restaurant", color = "#FF7043", type = CategoryType.EXPENSE, userId = SAMPLE_UID),
                        Category(name = "Di chuyển", icon = "directions_car", color = "#42A5F5", type = CategoryType.EXPENSE, userId = SAMPLE_UID),
                        Category(name = "Mua sắm", icon = "shopping_bag", color = "#AB47BC", type = CategoryType.EXPENSE, userId = SAMPLE_UID),
                        Category(name = "Lương", icon = "payments", color = "#66BB6A", type = CategoryType.INCOME, userId = SAMPLE_UID)
                    )

                    defaultCategories.forEach { category ->
                        Log.d(TAG, "[Category] Đang add: ${category.name}, type=${category.type}")
                        categoriesRef.add(category)
                            .addOnSuccessListener { docRef ->
                                Log.d(TAG, "[Category] ADD thành công: ${category.name}, id=${docRef.id}")
                                // Nếu là danh mục Ăn uống, tạo thêm 1 chi tiêu mẫu để người dùng thấy ngay
                                if (category.name == "Ăn uống") {
                                    seedSampleExpense(firestore, SAMPLE_UID, docRef.id)
                                }
                            }
                            .addOnFailureListener { e -> Log.e(TAG, "[Category] ADD THẤT BẠI (${category.name}): ${e.message}", e) }
                    }
                }
            }
            .addOnFailureListener { e -> Log.e(TAG, "[Category] QUERY THẤT BẠI: ${e.message}", e) }

        // 3. Khởi tạo các dữ liệu độc lập khác (Nhóm, Nợ)
        seedIndependentData(firestore, SAMPLE_UID, force)
    }

    private fun seedSampleExpense(db: FirebaseFirestore, uid: String, categoryId: String) {
        Log.d(TAG, "[Expense] Đang add expense cho categoryId=$categoryId")
        val expense = Expense(
            amount = 50000L,
            description = "Ăn trưa văn phòng",
            date = Timestamp.now(),
            categoryId = categoryId,
            userId = uid,
            createdAt = Timestamp.now()
        )
        db.collection("expenses").add(expense)
            .addOnSuccessListener { docRef -> Log.d(TAG, "[Expense] ADD thành công, id=${docRef.id}") }
            .addOnFailureListener { e -> Log.e(TAG, "[Expense] ADD THẤT BẠI: ${e.message}", e) }
    }

    private fun seedIndependentData(db: FirebaseFirestore, uid: String, force: Boolean) {
        // Khởi tạo khoản nợ mẫu
        val debtsRef = db.collection("debts")
        debtsRef.whereEqualTo("creditorId", uid).get()
            .addOnSuccessListener { snap ->
                Log.d(TAG, "[Debt] QUERY thành công, isEmpty=${snap.isEmpty}, size=${snap.size()}")
                if (snap.isEmpty || force) {
                    val debt = Debt(
                        amount = 200000L,
                        description = "Cho mượn tiền mặt",
                        creditorId = uid,
                        status = DebtStatus.PENDING,
                        createdAt = Timestamp.now()
                    )
                    Log.d(TAG, "[Debt] Đang add debt, status=${debt.status}")
                    debtsRef.add(debt)
                        .addOnSuccessListener { docRef -> Log.d(TAG, "[Debt] ADD thành công, id=${docRef.id}") }
                        .addOnFailureListener { e -> Log.e(TAG, "[Debt] ADD THẤT BẠI: ${e.message}", e) }
                }
            }
            .addOnFailureListener { e -> Log.e(TAG, "[Debt] QUERY THẤT BẠI: ${e.message}", e) }

        // Khởi tạo nhóm mẫu
        val groupsRef = db.collection("groups")
        groupsRef.whereArrayContains("memberIds", uid).get()
            .addOnSuccessListener { snap ->
                Log.d(TAG, "[Group] QUERY thành công, isEmpty=${snap.isEmpty}, size=${snap.size()}")
                if (snap.isEmpty || force) {
                    val group = Group(
                        name = "Nhà trọ",
                        description = "Chi tiêu chung phòng",
                        memberIds = listOf(uid),
                        createdBy = uid,
                        createdAt = Timestamp.now()
                    )
                    Log.d(TAG, "[Group] Đang add group: ${group.name}")
                    groupsRef.add(group)
                        .addOnSuccessListener { docRef -> Log.d(TAG, "[Group] ADD thành công, id=${docRef.id}") }
                        .addOnFailureListener { e -> Log.e(TAG, "[Group] ADD THẤT BẠI: ${e.message}", e) }
                }
            }
            .addOnFailureListener { e -> Log.e(TAG, "[Group] QUERY THẤT BẠI: ${e.message}", e) }
    }
}
