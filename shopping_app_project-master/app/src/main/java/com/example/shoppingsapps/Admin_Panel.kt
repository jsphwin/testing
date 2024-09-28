package com.example.shoppingsapps
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.util.UUID

class Admin_Panel : AppCompatActivity(), ProductAdapter.OnDeleteClickListener{

    companion object {
        private val PICK_IMAGE_REQUEST = 1
        private val PICK_IMAGE_REQUEST_PRODUCT = 2

    }

    private lateinit var storageReference: StorageReference
    private lateinit var imageView: ImageView



    private var imageUri: Uri? = null
    private lateinit var productNameEditText: EditText
    private lateinit var productInfoEditText: EditText
    private lateinit var productPriceEditText: EditText
    private lateinit var productImageView: ImageView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var productRecyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)


        val llll = findViewById<Button>(R.id.llll)
        llll.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Initialize Firebase Storage
        val storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        // Set up the button click listener
        val uploadImageButton: TextView = findViewById(R.id.uploadImageButton)
        uploadImageButton.setOnClickListener {

            // Open the gallery to select an image
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Assuming you have an ImageView in your layout with the id "imageView"
        imageView = findViewById(R.id.imageView_banner)

        // Fetch and display the latest image from Firebase Storage
        displayLatestImage()




















        productNameEditText = findViewById(R.id.productNameEditText)
        productInfoEditText = findViewById(R.id.productInfoEditText)
        productPriceEditText = findViewById(R.id.productPriceEditText)
        productImageView = findViewById(R.id.productImageView)
        productRecyclerView = findViewById(R.id.productRecyclerView)

        databaseReference = FirebaseDatabase.getInstance().getReference("products")
        storageReference = FirebaseStorage.getInstance().reference

        val submitButton: Button = findViewById(R.id.submitButton)

        productImageView.setOnClickListener {
            openImagePicker2()
        }

        submitButton.setOnClickListener {
            uploadProduct()
        }

        // Set up RecyclerView
        val productList: MutableList<Product> = mutableListOf()
        productAdapter = ProductAdapter(productList)
        productRecyclerView.adapter = productAdapter
        productRecyclerView.layoutManager = LinearLayoutManager(this,  LinearLayoutManager.HORIZONTAL, false)

        // Attach ValueEventListener to fetch products from Firebase
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                productList.clear()

                for (productSnapshot in dataSnapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    product?.let {
                        productList.add(it)
                    }
                }

                // Notify the adapter that the data set has changed
                productAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                showToast("Failed to read value. ${databaseError.toException()}")
            }
        })


        productAdapter.onDeleteClickListener = this



    }


    private fun deletePreviousImage() {
        val previousImageRef = storageReference.child("common_folder")

        // Delete the entire "common_folder" directory
        previousImageRef.delete()
            .addOnSuccessListener {
                Log.d("AdminPanel", "Previous images deleted successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("AdminPanel", "Error deleting previous images", exception)
            }
    }

    private fun displayLatestImage() {
        val latestImageRef = storageReference.child("common_folder/latest_banner.jpg")
        val shimmerViewContainer: ShimmerFrameLayout = findViewById(R.id.shimmer_admin)


        latestImageRef.downloadUrl
            .addOnSuccessListener { imageUrl ->
                // Display the latest image using Picasso
                runOnUiThread {
                    Picasso.get()
                        .load(imageUrl)
                        .error(R.drawable.baseline_image_24)
                        .into(imageView)
                }
                shimmerViewContainer.stopShimmer()
                shimmerViewContainer.setShimmer(null)
            }
            .addOnFailureListener { exception ->
                Log.e("AdminPanel", "Error fetching latest image", exception)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        Log.d("AdminPanel", "onActivityResult called with requestCode: $requestCode")

        if (requestCode == PICK_IMAGE_REQUEST) {
            Log.d("AdminPanel", "PICK_IMAGE_REQUEST matched")

            if (resultCode == RESULT_OK) {
                Log.d("AdminPanel", "Result OK")

                if (data != null && data.data != null) {
                    val selectedImageUri: Uri = data.data!!

                    Log.d("AdminPanel", "Selected Image Uri: $selectedImageUri")

                    // Upload the new image under the "common_folder" folder
                    val imageRef = storageReference.child("common_folder/latest_banner.jpg")

                    // Delete the previous image before uploading a new one
                    deletePreviousImage()

                    imageRef.putFile(selectedImageUri)
                        .addOnSuccessListener { taskSnapshot ->
                            // Image uploaded successfully
                            taskSnapshot.storage.downloadUrl
                                .addOnSuccessListener { imageUrl ->
                                    // Load the image into the ImageView using Picasso directly from imageUrl
                                    runOnUiThread {
                                        Picasso.get()
                                            .load(imageUrl)
                                            .placeholder(R.drawable.ic_launcher_background)
                                            .error(R.drawable.ic_launcher_foreground)
                                            .into(imageView)

                                        runOnUiThread {
                                            Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    // Handle any errors during the download URL retrieval
                                    Log.e("AdminPanel", "Error getting download URL", exception)
                                    runOnUiThread {
                                        Toast.makeText(this, "Error getting download URL", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors during the upload
                            Log.e("AdminPanel", "Upload failed: ${exception.message}", exception)
                            runOnUiThread {
                                Toast.makeText(this, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Log.e("AdminPanel", "Data or data.data is null")
                }
            } else {
                Log.e("AdminPanel", "Result not OK (resultCode: $resultCode)")
            }
        }









        if (requestCode == PICK_IMAGE_REQUEST_PRODUCT && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            productImageView.setImageURI(imageUri)
        }
    }




















    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    private fun openImagePicker2() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_PRODUCT)
    }



    private fun uploadProduct() {
        val productName = productNameEditText.text.toString()
        val productInfo = productInfoEditText.text.toString()
        val productPrice = productPriceEditText.text.toString()

        if (productName.isNotEmpty() && productPrice.isNotEmpty() && productInfo.isNotEmpty() && imageUri != null) {
            val imageName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = storageReference.child("new products/$imageName")

            val uploadTask = imageRef.putFile(imageUri!!)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    val productId = databaseReference.push().key
                    val product = Product(productId, productName, productInfo, productPrice, downloadUrl)

                    if (productId != null) {
                        databaseReference.child(productId).setValue(product)
                        showToast("Product uploaded successfully!")
                    }

                    clearFields()
                }
            }.addOnFailureListener { exception ->
                showToast("Upload failed. Please try again.")
                // Handle errors
            }
        } else {
            showToast("Please fill in all the fields and select an image.")
        }
    }

    private fun clearFields() {
        productNameEditText.text.clear()
        productPriceEditText.text.clear()
        productInfoEditText.text.clear()
        imageUri = null
        productImageView.setImageResource(R.drawable.ic_launcher_background)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



    // Implement the onDeleteClick function
    override fun onDeleteClick(product: Product, position: Int) {
        // Handle the deletion from Firebase and update the RecyclerView
        deleteProduct(product, position)
    }

    // Function to delete a product from Firebase and update the RecyclerView
    // Function to delete a product from Firebase and update the RecyclerView
    private fun deleteProduct(product: Product, position: Int) {
        // Remove the item from the list
        productAdapter.products.removeAt(position)
        // Notify the adapter about the removal
        productAdapter.notifyItemRemoved(position)

        // Delete the product from Firebase
        val productId = product.id
        Log.d("Admin_Panel", "Product ID: $productId")
        if (productId != null) {
            databaseReference.child(productId).removeValue()
                .addOnSuccessListener {
                    showToast("Product deleted successfully!")
                }
                .addOnFailureListener { exception ->
                    showToast("Failed to delete product: ${exception.message}")
                    Log.e("Admin_Panel", "Firebase Deletion Error", exception)
                }
        }
    }





}