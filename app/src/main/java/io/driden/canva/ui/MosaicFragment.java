package io.driden.canva.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.driden.canva.R;
import io.driden.canva.Utils.BitmapUtils;
import io.driden.canva.contract.MainContract;
import io.driden.canva.presenter.MainFragmentPresenter;

import static android.app.Activity.RESULT_OK;

public class MosaicFragment extends Fragment implements MainContract.View {

    final String TAG = getClass().getSimpleName();

    // code for Runtime permission (READ_EXTERNAL_STORAGE)
    final int PERMISSION_CODE = 42;

    // Intent Result Activity Code for open gallery
    final int RESULT_SELECT_PICTURE = 1005;

    // Usually a tile has the same length of width and height,
    // just in case, set two variables in order to make a tile with different width and height.
    int TILE_WIDTH = 32;
    int TILE_HEIGHT = 32;

    String filePath;    // a path of the selected image

    MainContract.Presenter presenter;   // Presenter

    // View unbinder of ButterKnife
    Unbinder unbinder;

    @BindView(R.id.image)
    ImageView mImageView;

    @BindView(R.id.tv_imgInfo)
    TextView tvImageInfo;

    /**
     * open the gallery
     */
    @OnClick(R.id.button_open_image)
    void openGallery() {
        if (!presenter.getIsFinished()) {
            Toast.makeText(getContext(), "The job is still running.", Toast.LENGTH_SHORT).show();
            return;
        }
        presenter.shutDownThreadExecutor();
        // Permission check for version 23 and higher versions.
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                // Permission Request
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_CODE);
            }
        }
        // Open the default gallery app.
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_SELECT_PICTURE);
    }

    /**
     * call funtion creating mosaics on the image.
     */
    @OnClick(R.id.button_mosaic_16)
    void drawMosaicImage16() {
        presenter.drawMosaicImage(filePath, 16, 16);
    }

    @OnClick(R.id.button_mosaic_32)
    void drawMosaicImage32() {
        presenter.drawMosaicImage(filePath, TILE_WIDTH, TILE_HEIGHT);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        presenter = new MainFragmentPresenter(getActivity());

        View mView = inflater.inflate(R.layout.fragment_img, container, false);

        unbinder = ButterKnife.bind(this, mView);

        presenter.setView(this);

        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_img_download) {
            presenter.downloadImage(mImageView);
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.shutDownThreadExecutor();
        unbinder.unbind();  // unbind view
    }

    @Override
    public void updateImageView(final Bitmap bitmap, final boolean isFinished) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mImageView.setImageBitmap(bitmap);

                if (isFinished) {
                    Toast.makeText(getActivity(), "Creating mosaics has been done", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void updateTextInfo(String text) {
        tvImageInfo.setText(text);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                // if the permission was granted.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {

                }

                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // get the image file path after closing gallery activity.
            if (resultCode == RESULT_OK && requestCode == RESULT_SELECT_PICTURE && null != data) {

                Uri imageUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getActivity().getContentResolver().query(imageUri, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

                filePath = cursor.getString(columnIndex);

                cursor.close();

                DisplayMetrics metrics = getResources().getDisplayMetrics();

                presenter.checkBitmapSize(filePath);

                // set the resized bitmap in the ImageView.
                mImageView.setImageBitmap(BitmapUtils.resizeBitmap(filePath, metrics));
                mImageView.setTag(filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error", Toast.LENGTH_LONG).show();
        }
    }
}
