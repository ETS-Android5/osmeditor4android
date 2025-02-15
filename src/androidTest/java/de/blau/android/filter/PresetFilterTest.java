package de.blau.android.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;
import de.blau.android.App;
import de.blau.android.LayerUtils;
import de.blau.android.Main;
import de.blau.android.Map;
import de.blau.android.R;
import de.blau.android.TestUtils;
import de.blau.android.osm.Node;
import de.blau.android.osm.Tags;
import de.blau.android.prefs.Preferences;
import de.blau.android.propertyeditor.PropertyEditor;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PresetFilterTest {

    private Context         context         = null;
    private ActivityMonitor monitor         = null;
    private Instrumentation instrumentation = null;
    private UiDevice        device          = null;
    private Main            main            = null;
    private Map             map             = null;

    @Rule
    public ActivityTestRule<Main> mActivityRule = new ActivityTestRule<>(Main.class);

    /**
     * Pre-test setup
     */
    @Before
    public void setup() {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);
        context = instrumentation.getTargetContext();
        monitor = instrumentation.addMonitor(PresetFilterActivity.class.getName(), null, false);
        main = mActivityRule.getActivity();
        Preferences prefs = new Preferences(context);
        LayerUtils.removeImageryLayers(context);
        map = main.getMap();
        map.setPrefs(main, prefs);

        TestUtils.grantPermissons(device);
        TestUtils.dismissStartUpDialogs(device, main);
        TestUtils.stopEasyEdit(main);
        TestUtils.loadTestData(main, "test2.osm");
    }

    /**
     * Post-test teardown
     */
    @After
    public void teardown() {
        instrumentation.removeMonitor(monitor);
    }

    /**
     * Set preset filter to Church and select one
     */
    @Test
    public void presetFilterSelect() {
        TestUtils.unlock(device);
        TestUtils.clickOverflowButton(device);
        TestUtils.clickText(device, false, main.getString(R.string.menu_enable_presetfilter), true, false);
        TestUtils.clickResource(device, false, device.getCurrentPackageName() + ":id/tagFilterButton", true);
        Activity presetFilterActivity = instrumentation.waitForMonitorWithTimeout(monitor, 30000);
        assertTrue(presetFilterActivity instanceof PresetFilterActivity);
        assertTrue(TestUtils.clickText(device, false, "Facilities", true));
        assertTrue(TestUtils.clickText(device, false, "Place of Worship", true, true));
        assertTrue(TestUtils.clickText(device, false, "Church", true));
        TestUtils.sleep(2000);
        TestUtils.clickAtCoordinates(device, main.getMap(), 8.38819D, 47.38961D);
        assertTrue(TestUtils.findText(device, false, main.getString(R.string.actionmode_wayselect), 5000));
        assertEquals(206010144L, App.getLogic().getSelectedWay().getOsmId());
        TestUtils.clickUp(device);
        TestUtils.clickOverflowButton(device);
        TestUtils.clickText(device, false, main.getString(R.string.menu_enable_presetfilter), true, false);
    }

    /**
     * Set preset filter to Church and select one
     */
    @Test
    public void hiddenObjectWarning() {
        TestUtils.unlock(device);
        TestUtils.clickOverflowButton(device);
        TestUtils.clickText(device, false, main.getString(R.string.menu_enable_presetfilter), true, false);
        TestUtils.clickResource(device, false, device.getCurrentPackageName() + ":id/tagFilterButton", true);
        Activity presetFilterActivity = instrumentation.waitForMonitorWithTimeout(monitor, 30000);
        assertTrue(presetFilterActivity instanceof PresetFilterActivity);
        assertTrue(TestUtils.clickText(device, false, "Highways", true));
        assertTrue(TestUtils.clickText(device, false, "Waypoints", true, true));
        assertTrue(TestUtils.clickText(device, false, "Pedestrian Crossing", true));
        TestUtils.sleep(2000);
        TestUtils.clickAtCoordinates(device, main.getMap(),  8.3886622D, 47.3887223D, true);
        assertTrue(TestUtils.findText(device, false, main.getString(R.string.actionmode_nodeselect), 5000));
        assertEquals(289987514L, App.getLogic().getSelectedNode().getOsmId());

        TestUtils.drag(device, main.getMap(), 8.3886622D, 47.3887223D,  8.389D, 47.389D, false, 10);
        
        TestUtils.findText(device, false, main.getString(R.string.attached_object_warning_stop), 2000);
        assertTrue(TestUtils.clickText(device, false, main.getString(R.string.attached_object_warning_stop), true));
        
        TestUtils.clickUp(device);
        TestUtils.clickOverflowButton(device);
        TestUtils.clickText(device, false, main.getString(R.string.menu_enable_presetfilter), true, false);
    }

    /**
     * Set preset filter to Church and create one
     */
    @Test
    public void presetFilterCreate() {
        TestUtils.zoomToLevel(device, main, 21);
        TestUtils.unlock(device);
        TestUtils.clickOverflowButton(device);
        TestUtils.clickText(device, false, main.getString(R.string.menu_enable_presetfilter), true, false);
        TestUtils.clickResource(device, false, device.getCurrentPackageName() + ":id/tagFilterButton", true);
        Activity presetFilterActivity = instrumentation.waitForMonitorWithTimeout(monitor, 30000);
        instrumentation.removeMonitor(monitor);
        assertTrue(presetFilterActivity instanceof PresetFilterActivity);
        assertTrue(TestUtils.clickText(device, false, "Facilities", true));
        assertTrue(TestUtils.clickText(device, false, "Place of Worship", true, true));
        assertTrue(TestUtils.clickText(device, false, "Church", true));
        TestUtils.sleep(2000);

        TestUtils.clickSimpleButton(device);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_add_node_tags), true, false));
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.simple_add_node)));
        TestUtils.clickAtCoordinates(device, map, 8.3893454, 47.3901898, true);

        monitor = instrumentation.addMonitor(PropertyEditor.class.getName(), null, false);

        Activity propertyEditor = instrumentation.waitForMonitorWithTimeout(monitor, 30000);
        assertTrue(propertyEditor instanceof PropertyEditor);
        assertTrue(TestUtils.findText(device, false, main.getString(R.string.menu_tags), 5000));
        assertTrue(TestUtils.findText(device, false, "Church", 5000));

        TestUtils.clickHome(device, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_nodeselect)));
        device.waitForIdle();
        Node n = App.getLogic().getSelectedNode();
        assertNotNull(n);
        assertTrue(n.hasTag(Tags.KEY_AMENITY, "place_of_worship"));

        TestUtils.clickUp(device);
        TestUtils.clickOverflowButton(device);
        TestUtils.clickText(device, false, main.getString(R.string.menu_enable_presetfilter), true, false);
    }

    /**
     * Set preset filter to place or Worship group and create a church
     */
    @Test
    public void presetFilterCreateGroup() {
        TestUtils.zoomToLevel(device, main, 21);
        TestUtils.unlock(device);
        TestUtils.clickOverflowButton(device);
        TestUtils.clickText(device, false, main.getString(R.string.menu_enable_presetfilter), true, false);
        TestUtils.clickResource(device, false, device.getCurrentPackageName() + ":id/tagFilterButton", true);
        Activity presetFilterActivity = instrumentation.waitForMonitorWithTimeout(monitor, 30000);
        instrumentation.removeMonitor(monitor);
        assertTrue(presetFilterActivity instanceof PresetFilterActivity);
        assertTrue(TestUtils.clickText(device, false, "Facilities", true));
        assertTrue(TestUtils.longClickText(device, "Place of Worship"));
        TestUtils.sleep(2000);

        TestUtils.clickSimpleButton(device);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_add_node_tags), true, false));
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.simple_add_node)));
        TestUtils.clickAtCoordinates(device, map, 8.3893454, 47.3901898, true);

        monitor = instrumentation.addMonitor(PropertyEditor.class.getName(), null, false);

        Activity propertyEditor = instrumentation.waitForMonitorWithTimeout(monitor, 30000);
        assertTrue(propertyEditor instanceof PropertyEditor);
        assertTrue(TestUtils.findText(device, false, main.getString(R.string.tag_menu_preset), 5000));
        assertTrue(TestUtils.findText(device, false, "Mosque", 5000));
        assertTrue(TestUtils.clickText(device, false, "Church", false, false));
        assertTrue(TestUtils.findText(device, false, main.getString(R.string.menu_tags), 5000));
        assertTrue(TestUtils.findText(device, false, "Church", 5000));

        TestUtils.clickHome(device, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_nodeselect)));
        device.waitForIdle();
        Node n = App.getLogic().getSelectedNode();
        assertNotNull(n);
        assertTrue(n.hasTag(Tags.KEY_AMENITY, "place_of_worship"));

        TestUtils.clickUp(device);
        TestUtils.clickOverflowButton(device);
        TestUtils.clickText(device, false, main.getString(R.string.menu_enable_presetfilter), true, false);
    }
}
