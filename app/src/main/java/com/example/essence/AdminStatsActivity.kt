package com.example.essence

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class AdminStatsActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var tvTotalUsers: TextView
    private lateinit var tvSellersCount: TextView
    private lateinit var tvBuyerCount: TextView
    private lateinit var tvTotalProperties: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var tvApprovedCount: TextView
    private lateinit var tvRejectedCount: TextView
    private lateinit var tvChangesRequiredCount: TextView
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_stats)

        tvTotalUsers = findViewById(R.id.tvTotalUers)
        tvSellersCount = findViewById(R.id.tvSellersCount)
        tvBuyerCount = findViewById(R.id.tvBuyerCount)
        tvTotalProperties = findViewById(R.id.tvTotalProperties)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        tvApprovedCount = findViewById(R.id.tvApprovedCount)
        tvRejectedCount = findViewById(R.id.tvRejectedCount)
        tvChangesRequiredCount = findViewById(R.id.tvChangeRequired)

        bottomNavigationView = findViewById(R.id.bottomNavigation)

        db = FirebaseFirestore.getInstance()
        loadAdminStats()

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_properties-> {
                    val intent = Intent(this, AdminActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_stats -> {
                    true
                }
                else -> false
            }
        }
    }

    private fun loadAdminStats() {

        db.collection("users")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AdminStatsActivity", "Total users count listen failed.", e)
                    tvTotalUsers.text = "0"
                    tvSellersCount.text = "0"
                    tvBuyerCount.text = "0"
                    return@addSnapshotListener
                }
                val totalUserCount = snapshots?.size() ?: 0
                tvTotalUsers.text = totalUserCount.toString()

                db.collection("users").whereEqualTo("role", "seller")
                    .addSnapshotListener { sellerSnapshots, sellerError ->
                        if (sellerError != null) {
                            Log.w("AdminStatsActivity", "Sellers count listen failed.", sellerError)
                            tvSellersCount.text = "0"
                            return@addSnapshotListener
                        }
                        tvSellersCount.text = sellerSnapshots?.size()?.toString() ?: "0"
                    }

                db.collection("users").whereEqualTo("role", "buyer")
                    .addSnapshotListener { buyerSnapshots, buyerError ->
                        if (buyerError != null) {
                            Log.w("AdminStatsActivity", "Buyers count listen failed.", buyerError)
                            tvBuyerCount.text = "0"
                            return@addSnapshotListener
                        }
                        tvBuyerCount.text = buyerSnapshots?.size()?.toString() ?: "0"
                    }
            }

        db.collection("properties")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AdminStatsActivity", "Total properties count listen failed.", e)
                    tvTotalProperties.text = "0"
                    return@addSnapshotListener
                }
                tvTotalProperties.text = snapshots?.size()?.toString() ?: "0"
            }

        db.collection("properties")
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AdminStatsActivity", "Pending count listen failed.", e)
                    tvPendingCount.text = "0"
                    return@addSnapshotListener
                }
                tvPendingCount.text = snapshots?.size()?.toString() ?: "0"
            }

        db.collection("properties")
            .whereEqualTo("status", "approved")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AdminStatsActivity", "Approved count listen failed.", e)
                    tvApprovedCount.text = "0"
                    return@addSnapshotListener
                }
                tvApprovedCount.text = snapshots?.size()?.toString() ?: "0"
            }

        db.collection("properties")
            .whereEqualTo("status", "rejected")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AdminStatsActivity", "Rejected count listen failed.", e)
                    tvRejectedCount.text = "0"
                    return@addSnapshotListener
                }
                tvRejectedCount.text = snapshots?.size()?.toString() ?: "0"
            }

        db.collection("properties")
            .whereEqualTo("status", "changes_requested") // Ensure this status string is correct
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("AdminStatsActivity", "Changes Required count listen failed.", e)
                    tvChangesRequiredCount.text = "0"
                    return@addSnapshotListener
                }
                tvChangesRequiredCount.text = snapshots?.size()?.toString() ?: "0"
            }
    }
    override fun onResume() {
        super.onResume()
        bottomNavigationView.selectedItemId = R.id.nav_stats
    }
}

