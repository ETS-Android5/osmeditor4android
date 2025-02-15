# Multiselect

Selected elements can be moved by dragging in the touch area. Individual objects can be selected / de-selected by (single) tapping them. A double tap in an entry area will end the mode.

## Actions  

### ![Undo](../images/undolist_undo.png) Undo

Tapping the icon once will undo the last operation. A long press will display a list of the operations since the last save, if you have undone anything a corresponding "redo" action will be displayed. If the undo results in the current selected object being removed you will be returned to the main screen. *Some operations consist of multiple simpler actions that will be listed as individual items.*

### ![Properties](../images/tag_menu_tags.png) Properties

Starts the property editor on the current selection.

### ![Copy](../images/ic_menu_copy_holo_light.png) Copy

Copy the selected objects  to the internal copy and paste buffer.

### ![Cut](../images/ic_menu_cut_holo_light.png) Cut

Move the the selected objects to the internal copy and paste buffer effectively deleting them from the data.

### ![Delete](../images/tag_menu_delete.png) Delete

Remove the objects from the data.

### ![Merge](../images/tag_menu_merge.png) Merge ways

Merge multiple selected ways resulting in a single way. Ways will be reversed if necessary. This option will only be available if only ways with common start/end nodes are selected, or the selection is two closed ways (polygons), in the later case if the polygons do not have common nodes a multi-polygon relation will be created and the ways added as members. If post-merge tag conflicts are detected you will be alerted. 

### Add node at intersection

If two or more ways are selected and they intersect witout a common node, a new node will be added at the first intersection found.

### ![Relation](../images/relation.png) Create relation

Create a relation starting with the current selected elements. The relation type can be selected from a list generated from the available presets. Further objects can be added until the check button is clicked, then the tag editor will be started on the new relation. 

### Add to relation

Select a relation and add the selected objects as members. Further objects can be added until the check button is clicked, then the tag editor will be started on the relation. 

### Zoom to selection

Pan and zoom the map to the currently selected object.

### Search for objects

Search for OSM objects in the loaded data using JOSMs search expressions.

### Upload elements

Upload the selected elements (only available for new or modified elements).

### ![Help](../images/menu_help.png) Help

Start the Vespucci Help browser