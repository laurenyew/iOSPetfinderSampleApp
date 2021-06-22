package laurenyew.petadoptsampleapp.ui.images

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

sealed class ImageState {
    data class Success(val image: Bitmap) : ImageState()
    object Failed : ImageState()
    object Loading : ImageState()
    object Empty : ImageState()
}

fun loadPicture(url: String?): Flow<ImageState> =
    if (url != null) {
        callbackFlow {
            var picassoListener: Target? = object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    if (bitmap != null) {
                        offer(ImageState.Success(image = bitmap))
                    } else {
                        offer(ImageState.Failed)
                    }
                    close()
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    offer(ImageState.Failed)
                    close()
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    offer(ImageState.Loading)
                }
            }
            Picasso.get().load(url).into(picassoListener!!)

            awaitClose {
                picassoListener = null
            }
        }
    } else {
        flowOf(ImageState.Empty)
    }