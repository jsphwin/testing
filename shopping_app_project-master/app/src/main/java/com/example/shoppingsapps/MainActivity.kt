package com.example.shoppingsapps
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.soundcloud.android.crop.Crop
import com.squareup.picasso.Picasso
import java.io.File

class MainActivity : Activity(), ProductAdapter.OnCartIconClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageView: ImageView
    private lateinit var banner_imageview: ImageView
    private lateinit var storageRef: StorageReference
    private lateinit var profilePicRef: StorageReference

    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: DatabaseReference
    private lateinit var productRecyclerView2: RecyclerView
    private lateinit var productRecyclerView3: RecyclerView
    private lateinit var productAdapter: ProductAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        changeStatusBarColor("#ffffff") // Replace with your desired color code

        val shimmerViewContainer: ShimmerFrameLayout = findViewById(R.id.shimmer_view_container)
        shimmerViewContainer.startShimmer()


        val go_back = findViewById<Button>(R.id.go_back)
        val payment_layout = findViewById<LinearLayout>(R.id.payment_layout)
        val home_layout = findViewById<LinearLayout>(R.id.home_layout)
        go_back.setOnClickListener {
            payment_layout.visibility = View.GONE
            home_layout.visibility = View.VISIBLE
        }
        payment_layout.visibility = View.GONE




        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()


        val textView = findViewById<TextView>(R.id.name)
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userName = user.displayName
            textView.text = userName
        } else {
            // Handle the case where the user is not signed in
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        val logout = findViewById<ImageView>(R.id.logout)






        logout.setOnClickListener {

            signOutAndStartSignInActivity()
            finish()
        }



        imageView = findViewById(R.id.profile_picture_image_view)
        banner_imageview = findViewById(R.id.banner_imageview)

        // Get a reference to the Firebase Storage location where the profile picture will be stored
        storageRef = FirebaseStorage.getInstance().reference

        // Get a reference to the current user's profile picture in Firebase Storage
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        profilePicRef = storageRef.child("images/$userId.jpg")

        // Check if the user has a profile picture in Firebase Storage
        profilePicRef.downloadUrl.addOnSuccessListener { uri ->
            // If the user has a profile picture, load it into the ImageView using Glide
            Glide.with(this).load(uri).into(imageView)
        }.addOnFailureListener { exception ->
            // If the user doesn't have a profile picture, do nothing
            Log.d("ProfilePic", "Profile picture not available", exception)
        }



        val latestImageRef = storageRef.child("common_folder/latest_banner.jpg")

        latestImageRef.downloadUrl
            .addOnSuccessListener { imageUrl ->
                // Display the latest image using Picasso
                runOnUiThread {
                    Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.color.white)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(banner_imageview)
                    shimmerViewContainer.stopShimmer()
                    shimmerViewContainer.setShimmer(null)




                }
            }

            .addOnFailureListener { exception ->
                Log.e("AdminPanel", "Error fetching latest image", exception)

            }







        // Set up the button to open the image picker
        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }






        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        user.displayName
                        val name = user.displayName
                        val phone = document.get("phone") as String

                        val userInfoTextView = findViewById<TextView>(R.id.name21)
                        userInfoTextView.text = "Welcome $name\n \n Your Phone Number Is: $phone"



                    }
                }
                .addOnFailureListener { exception ->
                    // Failed to retrieve user data

                    Toast.makeText(baseContext, "Failed to retrieve user data",
                        Toast.LENGTH_SHORT).show()
                }
        }



        /*   val deleteButton = findViewById<Button>(R.id.remove)
           deleteButton.setOnClickListener {
               if (userId != null) {

                   storageRef = FirebaseStorage.getInstance().getReference("images/$userId.jpg")
                   storageRef.delete().addOnSuccessListener {
                       val databaseRef = FirebaseDatabase.getInstance().getReference("users/$userId")
                       val updates = HashMap<String, Any?>()
                       updates["profile_pic_url"] = null
                       imageView.setImageResource(R.drawable.ic_launcher_background)
                       Toast.makeText(this, "User profile removed successfully", Toast.LENGTH_SHORT).show()

                       databaseRef.updateChildren(updates).addOnSuccessListener {
                           // User profile updated successfully


                       }.addOnFailureListener {
                           // Failed to update user profile
                       }
                   }.addOnFailureListener {
                       // Failed to delete image file

                       Toast.makeText(this, "Failed to delete image file", Toast.LENGTH_SHORT).show()
                   }
               }
           }  */




        databaseReference = FirebaseDatabase.getInstance().getReference("products")
        storageReference = FirebaseStorage.getInstance().reference
        productRecyclerView2 = findViewById(R.id.productRecyclerView2)
        productRecyclerView3 = findViewById(R.id.productRecyclerView3)
        // Set up RecyclerView
        val productList: MutableList<Product> = mutableListOf()
        productAdapter = ProductAdapter(productList)
        productAdapter.onCartIconClickListener = this
        productRecyclerView2.adapter = productAdapter
        productRecyclerView3.adapter = productAdapter
        productRecyclerView2.layoutManager = LinearLayoutManager(this,  LinearLayoutManager.HORIZONTAL, false)
        productRecyclerView3.layoutManager = LinearLayoutManager(this,  LinearLayoutManager.HORIZONTAL, false)


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

                productAdapter.isDeleteButtonVisible = false
                // Notify the adapter that the data set has changed
                productAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })











    }




    private fun signOutAndStartSignInActivity() {
        mAuth.signOut()
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            // Get the URI of the selected image
            val imageUri = data.data


            // Start the crop activity
            Crop.of(imageUri, Uri.fromFile(File(this.getExternalCacheDir(), "cropped_image.jpg")))
                .asSquare().start(this)


            Handler().postDelayed({
                // Upload the cropped image to Firebase Storage
                val file = File(this.getExternalCacheDir(), "cropped_image.jpg")

                val uploadTask = profilePicRef.putFile(Uri.fromFile(file))

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation profilePicRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        Glide.with(this).load(downloadUri).into(imageView)
                    } else {
                        // Handle errors here
                        Log.d("UploadError", task.exception.toString())
                    }
                }


            }, 5000) // 5000 milliseconds = 5 seconds


        }

    }



    private fun changeStatusBarColor(color: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = android.graphics.Color.parseColor(color)
        }
    }

    override fun onCartIconClick(product: Product) {
        val home_layout = findViewById<LinearLayout>(R.id.home_layout)
        val payment_layout = findViewById<LinearLayout>(R.id.payment_layout)
        payment_layout.visibility = View.VISIBLE
        home_layout.visibility = View.GONE
        val new_iv = findViewById<ImageView>(R.id.new_iv)
        val new_info_tv = findViewById<TextView>(R.id.new_info_tv)
        val new_name_tv = findViewById<TextView>(R.id.new_name_tv)
        val new_price_tv = findViewById<TextView>(R.id.new_price_tv)
        // Handle the click on the cart icon
        // Load the image into another ImageView
        Glide.with(this).load(product.imageUrl).into(new_iv)

        // Set name, description, price to other TextViews
        new_name_tv.text = product.productName
        new_info_tv.text = product.productInfo
        new_price_tv.text = product.productPrice
    }


}