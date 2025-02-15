package de.blau.android.easyedit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

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
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import de.blau.android.App;
import de.blau.android.LayerUtils;
import de.blau.android.Logic;
import de.blau.android.Main;
import de.blau.android.Map;
import de.blau.android.R;
import de.blau.android.TestUtils;
import de.blau.android.osm.OsmElement;
import de.blau.android.osm.Relation;
import de.blau.android.osm.RelationMember;
import de.blau.android.osm.Tags;
import de.blau.android.osm.Way;
import de.blau.android.prefs.AdvancedPrefDatabase;
import de.blau.android.prefs.Preferences;
import de.blau.android.propertyeditor.PropertyEditor;
import de.blau.android.propertyeditor.PropertyEditorTest;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RelationTest {

    Context              context         = null;
    AdvancedPrefDatabase prefDB          = null;
    Main                 main            = null;
    UiDevice             device          = null;
    Map                  map             = null;
    Logic                logic           = null;
    Instrumentation      instrumentation = null;

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
        main = mActivityRule.getActivity();
        Preferences prefs = new Preferences(context);
        LayerUtils.removeImageryLayers(context);
        prefs.enableSimpleActions(true);
        main.runOnUiThread(() -> main.showSimpleActionsButton());
        map = main.getMap();
        map.setPrefs(main, prefs);
        TestUtils.grantPermissons(device);
        TestUtils.dismissStartUpDialogs(device, main);
        logic = App.getLogic();
        logic.deselectAll();
        TestUtils.loadTestData(main, "test2.osm");
        TestUtils.stopEasyEdit(main);
    }

    /**
     * Post-test teardown
     */
    @After
    public void teardown() {
        TestUtils.stopEasyEdit(main);
        TestUtils.zoomToNullIsland(logic, map);
    }

    /**
     * Select, show info dialog, delete, undelete
     */
    // @SdkSuppress(minSdkVersion = 26)
    @Test
    public void selectRelation() {
        map.getDataLayer().setVisible(true);
        TestUtils.unlock(device);
        TestUtils.zoomToLevel(device, main, 21);
        TestUtils.clickAtCoordinates(device, map, 8.3893820, 47.3895626, true);
        assertTrue(TestUtils.clickText(device, false, "Hiking", false, false));
        List<Relation> rels = App.getLogic().getSelectedRelations();
        assertNotNull(rels);
        assertEquals(1, rels.size());
        Relation relation = rels.get(0);
        List<RelationMember> origMembers = relation.getMembers();
        assertEquals(6490362L, relation.getOsmId());
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_relationselect)));
        assertTrue(TestUtils.clickOverflowButton(device));
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_information), true, false));
        assertTrue(TestUtils.findText(device, false, "hiking"));
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.done), true, false));
        assertTrue(TestUtils.clickMenuButton(device, context.getString(R.string.delete), false, true));
        assertEquals(OsmElement.STATE_DELETED, relation.getState());
        assertTrue(TestUtils.clickMenuButton(device, context.getString(R.string.undo), false, false));
        assertEquals(OsmElement.STATE_UNCHANGED, relation.getState());
        TestUtils.clickText(device, true, context.getString(R.string.okay), true, false); // for the tip alert
        assertNotNull(relation.getMember(Way.NAME, 104148456L));
        List<RelationMember> members = relation.getMembers();
        assertEquals(origMembers.size(), members.size());
        for (int i = 0; i < members.size(); i++) {
            assertEquals(origMembers.get(i), members.get(i));
        }
    }

    /**
     * Select way, create relation, set tag, delete
     */
    // @SdkSuppress(minSdkVersion = 26)
    @Test
    public void createRelation() {
        map.getDataLayer().setVisible(true);
        TestUtils.unlock(device);
        TestUtils.zoomToLevel(device, main, 21);
        TestUtils.clickAtCoordinates(device, map, 8.3893820, 47.3895626, true);
        assertTrue(TestUtils.findText(device, false, "Path", 2000));
        assertTrue(TestUtils.clickText(device, false, "Path", false, false));
        Way way = App.getLogic().getSelectedWay();
        assertNotNull(way);
        assertEquals(104148456L, way.getOsmId());
        //
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_wayselect)));
        assertTrue(TestUtils.clickOverflowButton(device));
        TestUtils.scrollTo(context.getString(R.string.menu_relation), false);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_relation), true, false));
        TestUtils.scrollToEnd(true);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.select_relation_type_other), true, false));
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.menu_add_relation_member)));
        ActivityMonitor monitor = instrumentation.addMonitor(PropertyEditor.class.getName(), null, false);
        // finish
        TestUtils.clickButton(device, device.getCurrentPackageName() + ":id/simpleButton", true);
        Activity propertyEditor = instrumentation.waitForMonitorWithTimeout(monitor, 30000);
        assertNotNull(propertyEditor);
        TestUtils.sleep(5000);

        assertTrue(TestUtils.findText(device, false, "Relation type"));
        UiObject2 relationType = null;
        try {
            relationType = PropertyEditorTest.getField(device, "Relation type", 1);
        } catch (UiObjectNotFoundException e) {
            fail();
        }
        assertNotNull(relationType);
        relationType.click(); // NOSONAR
        relationType.setText(Tags.VALUE_MULTIPOLYGON); // can't find text in drop downs
        TestUtils.clickHome(device, true);
        List<Relation> relations = App.getLogic().getSelectedRelations();
        assertEquals(1, relations.size());
        Relation relation = relations.get(0);
        assertTrue(relation.getOsmId() < 0);
        assertTrue(relation.hasTag(Tags.KEY_TYPE, Tags.VALUE_MULTIPOLYGON));
        assertTrue(TestUtils.clickMenuButton(device, "Delete", false, true));
    }

    /**
     * Create way, create multipolygon, check roles
     */
    // @SdkSuppress(minSdkVersion = 26)
    @Test
    public void createAndAddToMultiPolygon() {
        map.getDataLayer().setVisible(true);
        TestUtils.zoomToLevel(device, main, 22);
        TestUtils.unlock(device);
        // split building first
        TestUtils.clickAtCoordinates(device, map, 8.3882060, 47.3885768, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_wayselect)));
        assertTrue(TestUtils.clickMenuButton(device, "Split", false, true));
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_closed_way_split_1)));
        TestUtils.clickAtCoordinates(device, map, 8.3881251, 47.3885077, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_closed_way_split_2)));
        TestUtils.clickAtCoordinates(device, map, 8.3881577, 47.3886924, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_multiselect)));
        TestUtils.clickUp(device);
        //
        TestUtils.clickButton(device, device.getCurrentPackageName() + ":id/simpleButton", true);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_add_way), true, false));
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.simple_add_way)));
        TestUtils.clickAtCoordinates(device, map, 8.3880883, 47.3885877, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_createpath), 1000));
        TestUtils.clickAtCoordinates(device, map, 8.3881994, 47.3886178, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3881408, 47.3886491, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3880883, 47.3885877, true);
        TestUtils.clickButton(device, device.getCurrentPackageName() + ":id/simpleButton", true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.tag_form_untagged_element), 5000));
        TestUtils.clickHome(device, true); // exit property editor
        Way inner = App.getLogic().getSelectedWay();
        assertNotNull(inner);
        //
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_wayselect)));
        assertTrue(TestUtils.clickOverflowButton(device));
        TestUtils.scrollTo(context.getString(R.string.menu_relation), false);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.menu_relation), true, false));
        TestUtils.scrollTo("Multipolygon", true);
        assertTrue(TestUtils.clickText(device, false, "Multipolygon", true, false));
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.menu_add_relation_member)));
        TestUtils.clickAtCoordinates(device, map, 8.3882060, 47.3885768, true);
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3880720, 47.3886182, true);
        TestUtils.sleep();
        // finish
        TestUtils.clickButton(device, device.getCurrentPackageName() + ":id/simpleButton", true);
        ActivityMonitor monitor = instrumentation.addMonitor(PropertyEditor.class.getName(), null, false);
        // assertTrue(TestUtils.findText(device, false, context.getString(R.string.move), 5000));
        // assertTrue(TestUtils.clickText(device, false, context.getString(R.string.move), true, false));
        TestUtils.sleep(2000);
        TestUtils.clickButton(device, "android:id/button1", true);
        Activity propertyEditor = instrumentation.waitForMonitorWithTimeout(monitor, 30000);
        assertNotNull(propertyEditor);
        TestUtils.sleep(5000);
        TestUtils.clickHome(device, true);
        List<Relation> relations = App.getLogic().getSelectedRelations();
        assertEquals(1, relations.size());
        Relation relation = relations.get(0);
        assertTrue(relation.getOsmId() < 0);
        assertTrue(relation.hasTag(Tags.KEY_TYPE, Tags.VALUE_MULTIPOLYGON));
        assertTrue(relation.hasTag(Tags.KEY_BUILDING, "residential"));
        List<RelationMember> members = relation.getMembers();
        assertNotNull(members);
        assertEquals(3, members.size());
        RelationMember innerMember = relation.getMember(inner);
        assertEquals(Tags.ROLE_INNER, innerMember.getRole());
        List<RelationMember> outerMembers = relation.getMembersWithRole(Tags.ROLE_OUTER);
        assertEquals(2, outerMembers.size());
        RelationMember outer = outerMembers.get(0);
        assertTrue(outer.getElement().getTags().isEmpty()); // tags have been moved
        // select another building add add it to the MP
        TestUtils.sleep();
        TestUtils.clickAtCoordinates(device, map, 8.3884429, 47.3887624, true);
        assertTrue(TestUtils.findText(device, false, context.getString(R.string.actionmode_wayselect)));
        assertTrue(TestUtils.clickOverflowButton(device));
        TestUtils.scrollTo(context.getString(R.string.tag_menu_addtorelation), true);
        assertTrue(TestUtils.clickText(device, false, context.getString(R.string.tag_menu_addtorelation), true, false));
        assertTrue(TestUtils.clickText(device, false, "Multipolygon Building", true, false));
        // finish again
        TestUtils.clickButton(device, device.getCurrentPackageName() + ":id/simpleButton", true);
        // assertTrue(TestUtils.findText(device, false, context.getString(R.string.remove), 5000));
        // assertTrue(TestUtils.clickText(device, false, context.getString(R.string.remove), true, false));
        TestUtils.sleep(2000);
        TestUtils.clickButton(device, "android:id/button1", true);
        TestUtils.sleep(2000);
        TestUtils.clickHome(device, true); // exit property editor
        outerMembers = relation.getMembersWithRole(Tags.ROLE_OUTER);
        assertEquals(3, outerMembers.size());
        outer = outerMembers.get(2);
        assertEquals(1, outer.getElement().getTags().size());
        assertTrue(outer.getElement().hasTagWithValue(Tags.KEY_ADDR_HOUSENUMBER, "38"));
    }
}
