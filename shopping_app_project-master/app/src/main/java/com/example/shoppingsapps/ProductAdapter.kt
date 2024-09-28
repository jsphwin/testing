package com.example.shoppingsapps
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductAdapter( val products: MutableList<Product>) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    interface OnDeleteClickListener {
        fun onDeleteClick(product: Product, position: Int)
    }

    interface OnCartIconClickListener {
        fun onCartIconClick(product: Product)
    }

    var onDeleteClickListener: OnDeleteClickListener? = null
    var onCartIconClickListener: OnCartIconClickListener? = null
    var isDeleteButtonVisible: Boolean = true


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productNameTextView)
        val productInfo: TextView = itemView.findViewById(R.id.productInfoTextView)
        val productPrice: TextView = itemView.findViewById(R.id.productPriceTextView)
        val productImage: ImageView = itemView.findViewById(R.id.productImageView)
        val cartIcon: ImageView = itemView.findViewById(R.id.buy_button)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = products[position]
        holder.productName.text = product.productName
        holder.productPrice.text = product.productPrice
        holder.productInfo.text = product.productInfo

        // Load image using a library like Picasso or Glide
        // Example with Glide:
        Glide.with(holder.productImage.context)
            .load(product.imageUrl)
            .into(holder.productImage)

        if (isDeleteButtonVisible) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDeleteClickListener?.onDeleteClick(product, position)
            }
        } else {
            // If isDeleteButtonVisible is false, hide the delete button
            holder.deleteButton.visibility = View.GONE
        }

        holder.cartIcon.setOnClickListener {
            onCartIconClickListener?.onCartIconClick(product)
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }
}





