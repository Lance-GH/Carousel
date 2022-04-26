package com.example.carousel

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.example.carousel.databinding.ActivityGridBinding


class GridActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_POSITION = "EXTRA_POSITION"
        const val EXTRA_IMAGES = "EXTRA_IMAGES"
    }

    private lateinit var binding: ActivityGridBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGridBinding.inflate(layoutInflater)
        setContentView(binding.root)


        with(binding.recyclerView) {
            layoutManager = GridLayoutManager(this@GridActivity, 5)
            addItemDecoration(
                GridSpacingDecoration(resources.getDimensionPixelSize(R.dimen.grid_spacing))
            )

            reloadImages()
        }
    }

    /** Display a random set of images each time */
    private fun reloadImages() {
        val images: ArrayList<Image> = ArrayList(Data.images.shuffled())
        binding.recyclerView.swapAdapter(GridAdapter(images) { _, position ->
            startActivity(
                Intent(this@GridActivity, CarouselActivity::class.java)
                    .putParcelableArrayListExtra(EXTRA_IMAGES, images)
                    .putExtra(EXTRA_POSITION, position)
            )
        }, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.grid_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_item_shuffle -> {
                reloadImages()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

class GridSpacingDecoration(
    @Px private val spacing: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val itemPosition = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        val lm = parent.layoutManager as GridLayoutManager
        val spanCount = lm.spanCount
        val spanIndex = lm.spanSizeLookup.getSpanIndex(itemPosition, spanCount)

        val isTopRow = itemPosition < spanCount

        val bottomRowIsComplete = itemCount % spanCount == 0
        val isBottomRow = if (bottomRowIsComplete) itemPosition >= itemCount - spanCount else itemPosition >= itemCount - (itemCount % spanCount)

        outRect.top = if (isTopRow) spacing else spacing / 2
        outRect.bottom = if (isBottomRow) spacing else spacing / 2
        outRect.left = (spacing * ((spanCount - spanIndex) / spanCount.toFloat())).toInt()
        outRect.right = (spacing * ((spanIndex + 1) / spanCount.toFloat())).toInt()
    }
}

class GridAdapter(
    private val images: List<Image>,
    private val onImageClicked: (Image, Int) -> Unit
) : RecyclerView.Adapter<GridAdapter.VH>() {
    init {
        setHasStableIds(true);
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(parent)

    override fun onBindViewHolder(holder: VH, position: Int) {
        val image = images[position]

        Glide.with(holder.imageView)
            .load(image.url)
            .transition(withCrossFade())
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            onImageClicked(image, position)
        }
    }

    override fun getItemCount(): Int = images.size

    override fun getItemId(position: Int): Long {
        return images[position].url.hashCode().toLong()
    }

    class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_grid_image, parent, false)
    ) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
    }
}