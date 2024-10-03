package app.organicmaps.downloader;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import app.organicmaps.util.WindowInsetUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import app.organicmaps.R;
import app.organicmaps.util.StringUtils;
import app.organicmaps.util.UiUtils;
import static app.organicmaps.downloader.CountryItem.*;

class BottomPanel
{
  private final DownloaderFragment mFragment;
  private final FloatingActionButton mFab;
  private final Button mButton;

  private final View.OnClickListener mDownloadListener = new View.OnClickListener()
  {
    @Override
    public void onClick(View v)
    {
      MapManager.warn3gAndDownload(mFragment.requireActivity(), mFragment.getCurrentRoot(), null);
    }
  };

  private final View.OnClickListener mUpdateListener = new View.OnClickListener()
  {
    @Override
    public void onClick(View v)
    {
      final String country = mFragment.getCurrentRoot();
      MapManager.warnOn3gUpdate(mFragment.requireActivity(), country, () -> MapManager.nativeUpdate(country));
    }
  };

  private final View.OnClickListener mCancelListener = new View.OnClickListener()
  {
    @Override
    public void onClick(View v)
    {
        MapManager.nativeCancel(mFragment.getCurrentRoot());
    }
  };

  BottomPanel(DownloaderFragment fragment, View frame)
  {
    mFragment = fragment;

    mFab = frame.findViewById(R.id.fab);
    mFab.setOnClickListener(v -> {
      if (mFragment.getAdapter() != null )
        mFragment.getAdapter().setAvailableMapsMode();
      update();
    });

    mButton = frame.findViewById(R.id.action);

    ViewCompat.setOnApplyWindowInsetsListener(frame, new OnApplyWindowInsetsListener()
    {
      @NonNull
      @Override
      public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets)
      {
        Insets safeInsets = insets.getInsets(WindowInsetUtils.TYPE_SAFE_DRAWING);
        int baseMargin = UiUtils.dimen(v.getContext(), R.dimen.margin_base);

        ViewGroup.MarginLayoutParams fabParams = (ViewGroup.MarginLayoutParams) mFab.getLayoutParams();
        ViewGroup.MarginLayoutParams buttonParams = (ViewGroup.MarginLayoutParams) mButton.getLayoutParams();

        final boolean isButtonVisible = UiUtils.isVisible(mButton);

        buttonParams.bottomMargin = safeInsets.bottom;
        mButton.setPadding(safeInsets.left, mButton.getPaddingTop(), safeInsets.right, mButton.getPaddingBottom());

        fabParams.rightMargin = safeInsets.right + baseMargin;
        if (isButtonVisible)
        {
          fabParams.bottomMargin = baseMargin;
        }
        else
        {
          fabParams.bottomMargin = safeInsets.bottom + baseMargin;
        }

        mFab.requestLayout();
        mButton.requestLayout();

        return insets;
      }
    });
  }

  private void setUpdateAllState(UpdateInfo info)
  {
    mButton.setText(StringUtils.formatUsingUsLocale("%s (%s)", mFragment.getString(R.string.downloader_update_all_button),
                                  StringUtils.getFileSizeString(mFragment.requireContext(), info.totalSize)));
    mButton.setOnClickListener(mUpdateListener);
  }

  private void setDownloadAllState()
  {
    mButton.setText(R.string.downloader_download_all_button);
    mButton.setOnClickListener(mDownloadListener);
  }

  private void setCancelState()
  {
    mButton.setText(R.string.downloader_cancel_all);
    mButton.setOnClickListener(mCancelListener);
  }

  public void update()
  {
    DownloaderAdapter adapter = mFragment.getAdapter();
    boolean search = adapter.isSearchResultsMode();

    boolean show = !search;
    UiUtils.showIf(show && adapter.isMyMapsMode(), mFab);

    if (show)
    {
      String root = adapter.getCurrentRootId();
      int status = MapManager.nativeGetStatus(root);
      if (adapter.isMyMapsMode())
      {
        switch (status)
        {
          case STATUS_UPDATABLE ->
          {
            UpdateInfo info = MapManager.nativeGetUpdateInfo(root);
            setUpdateAllState(info);
          }  // Special case for "Countries" node when no maps currently downloaded.
          case STATUS_DOWNLOADABLE, STATUS_DONE, STATUS_PARTLY -> show = false;
          case STATUS_PROGRESS, STATUS_APPLYING, STATUS_ENQUEUED -> setCancelState();
          case STATUS_FAILED -> setDownloadAllState();
          default -> throw new IllegalArgumentException("Inappropriate status for \"" + root + "\": " + status);
        }
      }
      else
      {
        show = !CountryItem.isRoot(root);
        if (show)
        {
          switch (status)
          {
            case STATUS_UPDATABLE ->
            {
              UpdateInfo info = MapManager.nativeGetUpdateInfo(root);
              setUpdateAllState(info);
            }
            case STATUS_DONE -> show = false;
            case STATUS_PROGRESS, STATUS_APPLYING, STATUS_ENQUEUED -> setCancelState();
            default -> setDownloadAllState();
          }
        }
      }
    }

    UiUtils.showIf(show, mButton);
  }
}
