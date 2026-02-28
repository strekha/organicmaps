package app.organicmaps.car.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.UiThread;
import androidx.car.app.CarContext;
import app.organicmaps.R;
import app.organicmaps.sdk.MapStyle;
import app.organicmaps.sdk.routing.RoutingController;

public final class ThemeUtils
{
  public enum ThemeMode
  {
    AUTO(R.string.auto, "auto"),
    LIGHT(R.string.off, "default"),
    NIGHT(R.string.on, "night");

    ThemeMode(@StringRes int titleId, @NonNull String value)
    {
      mTitleId = titleId;
      mValue = value;
    }

    @StringRes
    public int getTitleId()
    {
      return mTitleId;
    }

    @NonNull
    private String getValue()
    {
      return mValue;
    }

    @StringRes
    private final int mTitleId;
    @NonNull
    private final String mValue;
  }

  private static final String ANDROID_AUTO_PREFERENCES_FILE_KEY = "ANDROID_AUTO_PREFERENCES_FILE_KEY";
  private static final String THEME_KEY = "ANDROID_AUTO_THEME_MODE";

  @UiThread
  public static void update(@NonNull CarContext context)
  {
    final ThemeMode oldThemeMode = getThemeMode(context);
    update(context, oldThemeMode);
  }

  @UiThread
  public static void update(@NonNull CarContext context, @NonNull ThemeMode oldThemeMode)
  {
    final ThemeMode newThemeMode =
        oldThemeMode == ThemeMode.AUTO ? (context.isDarkMode() ? ThemeMode.NIGHT : ThemeMode.LIGHT) : oldThemeMode;

    MapStyle newMapStyle;
    if (newThemeMode == ThemeMode.NIGHT)
      newMapStyle = RoutingController.get().isVehicleNavigation() ? MapStyle.VehicleDark : MapStyle.Dark;
    else
      newMapStyle = RoutingController.get().isVehicleNavigation() ? MapStyle.VehicleClear : MapStyle.Clear;

    if (MapStyle.get() != newMapStyle)
      MapStyle.set(newMapStyle);
  }

  @SuppressLint("ApplySharedPref")
  @UiThread
  public static void setThemeMode(@NonNull CarContext context, @NonNull ThemeMode themeMode)
  {
    getSharedPreferences(context).edit().putString(THEME_KEY, themeMode.getValue()).commit();
    update(context, themeMode);
  }

  @NonNull
  public static ThemeMode getThemeMode(@NonNull CarContext context)
  {
    final var savedValue = getSharedPreferences(context).getString(THEME_KEY, ThemeMode.AUTO.getValue());
    return switch (savedValue)
    {
      case "default" -> ThemeMode.LIGHT;
      case "night" -> ThemeMode.NIGHT;
      default -> ThemeMode.AUTO;
    };
  }

  @NonNull
  private static SharedPreferences getSharedPreferences(@NonNull CarContext context)
  {
    return context.getSharedPreferences(ANDROID_AUTO_PREFERENCES_FILE_KEY, Context.MODE_PRIVATE);
  }

  private ThemeUtils() {}
}
