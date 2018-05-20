package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat.IntentBuilder;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ArticleLoader.Query;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private CoordinatorLayout mCoordinatorLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ImageView mImageViewDisp;
    private FloatingActionButton fabShareButtonDisp;
    private TextView textViewByLine;
    private TextView textViewBodyContentDisp;
    private TextView textViewTitleDisp;
    private LinearLayout metaBar;
    private Toolbar mToolbar;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);

            setHasOptionsMenu(true);
        }
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        /*Implementing Coordinator Layout & Collapsing ToolBar Layout*/
        mCoordinatorLayout = (CoordinatorLayout)
                mRootView.findViewById(R.id.draw_insets_frame_layout);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) mRootView.findViewById(R.id.photo_container);
        mImageViewDisp = (ImageView) mRootView.findViewById(R.id.photo);
        fabShareButtonDisp = (FloatingActionButton) mRootView.findViewById(R.id.share_fab);
        /* Text Views to Display the Text Content in screen
           1.Article Title
           2.Aritlce ByLine
           3.Article Body*/
        textViewTitleDisp = (TextView) mRootView.findViewById(R.id.article_title);
        textViewByLine = (TextView) mRootView.findViewById(R.id.article_byline);
        textViewBodyContentDisp = (TextView) mRootView.findViewById(R.id.article_body);
        /*Linear Layout Display*/
        metaBar = (LinearLayout) mRootView.findViewById(R.id.meta_bar);
        /*Creating ToolBar*/
        mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);


        getLoaderManager().initLoader(0, null, this);

        return mRootView;
    }
/*Implementing Animation to Fab Share Button*/
    @Override
    public void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fabShareButtonDisp.setAlpha(0f);
            fabShareButtonDisp.setScaleX(0f);
            fabShareButtonDisp.setScaleY(0f);
            fabShareButtonDisp.setTranslationZ(1f);
            fabShareButtonDisp.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationZ(25f)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .setStartDelay(300)
                    .start();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }
    
    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Cursor Item Reading Error");
            mCursor.close();
            mCursor = null;
        }

        Typeface mainTypeface = Typeface.createFromAsset(getResources().getAssets(), "Roboto-Regular.ttf");


        if (mCursor != null) {
            mRootView.setVisibility(View.VISIBLE);
            final String title = mCursor.getString(Query.TITLE);

            textViewByLine.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(Query.AUTHOR)
                            + "</font>"));
            textViewBodyContentDisp.setText(Html.fromHtml(mCursor.getString(Query.BODY)));
            if (mToolbar != null) {
                ((ArticleDetailActivity) getActivity()).setSupportActionBar(mToolbar);
                ((ArticleDetailActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
                mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                mToolbar.setNavigationOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getActivity().finish();
                    }
                });
            }

            textViewTitleDisp.setText(title);
            textViewBodyContentDisp.setTypeface(mainTypeface);
            textViewByLine.setTypeface(mainTypeface);
            textViewTitleDisp.setTypeface(mainTypeface);
            /*Display image and apply Image color to Status bar Dinamically with pallet*/
            Glide.with(getActivity())
                    .load(mCursor.getString(Query.PHOTO_URL))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .dontAnimate()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            Bitmap bitmap = ((GlideBitmapDrawable) resource.getCurrent()).getBitmap();
                            Palette.from(bitmap).generate(new PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    int defaultDispColor = 0xFF333333;
                                    int mDarkVibrant = palette.getDarkVibrantColor(defaultDispColor);
                                    metaBar.setBackgroundColor(mDarkVibrant);
                                    if (mCollapsingToolbarLayout != null) {
                                        int mDarkMuted = palette.getDarkMutedColor(defaultDispColor);
                                        mCollapsingToolbarLayout.setContentScrimColor(mDarkMuted);
                                        mCollapsingToolbarLayout.setStatusBarScrimColor(mDarkMuted);
                                    }
                                }
                            });


                            return false;
                        }
                    })
                    .into(mImageViewDisp);

            /*Implementing onClick Listener to the FabShareButton*/
            fabShareButtonDisp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(Intent.createChooser(IntentBuilder.from(getActivity())
                            .setType("text/plain")
                            .setText("Some sample text")
                            .getIntent(), getString(R.string.action_share)));

                }
            });
        } else {
            Snackbar.make(mCoordinatorLayout, R.string.error_message, Snackbar.LENGTH_LONG).show();
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
    }

}
