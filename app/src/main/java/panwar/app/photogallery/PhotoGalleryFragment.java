package panwar.app.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import panwar.app.photogallery.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";
    private int lastFetchedPage = 1;
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) v
                .findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                PhotoAdapter adapter = (PhotoAdapter)recyclerView.getAdapter(); // must be cast to Photoadapter, as I use a non-inherited method
                int lastPosition = adapter.getLastBoundPosition();
                GridLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
                int loadBufferPosition = 1;
                if(lastPosition >= adapter.getItemCount() - layoutManager.getSpanCount()- loadBufferPosition){
                    new FetchItemsTask().execute(lastPosition
                    +1);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }


        });
        setupAdapter();

        return v;
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);

            //mTitleTextView = (TextView) itemView;
            mItemImageView = (ImageView) itemView
                    .findViewById(R.id.fragment_photo_gallery_image_view);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            Picasso.with(getActivity())
                    .load(galleryItem.getUrl())
                    .placeholder(R.drawable.placeholder_image_logo)
                    .into(mItemImageView);
        }
    }
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;
        private int lastBoundPosition;
        public int getLastBoundPosition() {
            return lastBoundPosition;
        }
        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, viewGroup, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
           // Drawable placeholder = getResources().getDrawable(R.drawable.placeholder_image_logo);
            photoHolder.bindGalleryItem(galleryItem);
            lastBoundPosition = position;
            Log.i(TAG,"Last bound position is " + Integer.toString(lastBoundPosition));
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Integer,Void,List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            return new FlickrFetchr().fetchItems(lastFetchedPage);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            if(lastFetchedPage >1){
                mItems.addAll(items);
                mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
            }
            else{
                mItems = items;
                setupAdapter();
            }
            lastFetchedPage++;
        }

    }

}
