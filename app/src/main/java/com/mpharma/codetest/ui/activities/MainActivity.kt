package com.mpharma.codetest.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.mpharma.codetest.databinding.ActivityMainBinding
import com.mpharma.codetest.ui.screenstates.ScreenState
import com.mpharma.codetest.ui.adapters.ProductsAdapter
import com.mpharma.codetest.ui.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainActivityViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProductsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initAdapter()
        setupRecyclerView()
        setupFabClickListener()
        observeScreenState()
    }

    private fun initAdapter() {
        adapter =
            ProductsAdapter(onClick = { startDetailActivity(it.prices.first().productId) }) { productId ->
                mainViewModel.deleteProduct(productId)
            }
    }

    private fun setupRecyclerView() {
        with(binding) {
            productsRecyclerView.adapter = adapter
            productsRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            productsRecyclerView.addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
    }

    private fun observeScreenState() {
        lifecycleScope.launchWhenStarted {
            mainViewModel.state.collect { screenState ->
                handleScreenState(screenState)
            }
        }
    }

    private fun handleScreenState(state: ScreenState) {
        when (state) {
            is ScreenState.Success -> {
                binding.progressBar.visibility = View.GONE
                adapter.setData(state.data)
            }

            is ScreenState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
            }

            is ScreenState.Error -> {
                // TODO: SHOW ERROR
            }
        }
    }

    private fun setupFabClickListener() {
        binding.fab.setOnClickListener {
            startActivity(Intent(this, ProductFormActivity::class.java))
        }
    }

    private fun startDetailActivity(productId: String) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("product_id", productId)
        startActivity(intent)
    }
}