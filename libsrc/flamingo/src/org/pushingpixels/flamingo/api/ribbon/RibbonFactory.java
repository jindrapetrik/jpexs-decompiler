package org.pushingpixels.flamingo.api.ribbon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenu;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryFooter;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntrySecondary;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

/**
 * The <code>RibbonFactory</code> is an object which implements the <a
 * href="http://en.wikipedia.org/wiki/Builder_pattern">Builder Pattern</a>. The
 * factory provides methods for customizing a {@link JRibbon}. By using this
 * factory, you may save the amount of code you write and improve the
 * readability of your code.
 * <p>
 * Methods are provided for adding items to the ribbon being created by the
 * factory. After everything has been added and customization is finished, the
 * ribbon is accessed via the {@link #getRibbon()} method.
 * <p>
 * Due to the way the Flamingo ribbon API was implemented the factory utilizes
 * queues when adding components such as <code>JRibbonBand</code>,
 * <code>JCommandButton</code>, <code>RibbonApplicationMenuEntrySecondary</code>
 * , and <em>contextual</em> <code>RibbonTask</code>s.
 * <p>
 * <em><b>NOTE:</b> It is realized that the factory doesn't provide all features available if
 * customizing a ribbon from scratch, but enhancements are intended to be added
 * in the future.</em>
 * 
 * @author Erich Schroeter
 */
public class RibbonFactory {

	/** The ribbon to be returns after customization */
	private JRibbon ribbon;
	/** A queue of ribbon bands to add to the next ribbon task */
	private LinkedList<JRibbonBand> bands;
	/** A queue of command buttons to add to the next ribbon band */
	private LinkedList<JCommandButton> commandButtons;
	/** A mapping between buttons and the priority it needs to be in a band */
	private Map<JCommandButton, RibbonElementPriority> commandButtonsPriority;
	/** The title to use for the next sub group added */
	private String nextSubMenuTitle;
	/** A queue of sub menu items to add to the next primary menu item */
	private LinkedList<RibbonApplicationMenuEntrySecondary> subMenuItems;
	/**
	 * The ribbon menu for the application. This is not added unless
	 * {@link #withMenu()} or {@link #withMenu(ResizableIcon)} is called.
	 */
	private RibbonApplicationMenu applicationMenu;
	/** A queue of tasks to be added to a contextual task group */
	private LinkedList<RibbonTask> contextualTasks;

	/**
	 * Constructs a default <code>RibbonFactory</code> to build a
	 * {@link JRibbon}.
	 * <p>
	 * A default <code>JRibbon</code> is initialized and all factory queues are
	 * initialized. After all customization is finished the ribbon is accessed
	 * via the {@link #getRibbon()} method.
	 */
	public RibbonFactory() {
		this(new JRibbon());
	}

	/**
	 * Constructs a <code>RibbonFactory</code> to customize the specified
	 * <code>ribbon</code>.
	 * <p>
	 * All factory queues are initialized. After all customization is finished
	 * the ribbon is accessed via the {@link #getRibbon()} method.
	 * 
	 * @param ribbon
	 *            the existing ribbon
	 */
	public RibbonFactory(JRibbon ribbon) {
		this.ribbon = ribbon;
		bands = new LinkedList<JRibbonBand>();
		commandButtons = new LinkedList<JCommandButton>();
		commandButtonsPriority = new HashMap<JCommandButton, RibbonElementPriority>();
		subMenuItems = new LinkedList<RibbonApplicationMenuEntrySecondary>();
		if (ribbon.getApplicationMenu() == null) {
			applicationMenu = new RibbonApplicationMenu();
		}
		contextualTasks = new LinkedList<RibbonTask>();
	}

	/**
	 * Adds a {@link RibbonContextualTaskGroup} comprised of the
	 * <code>RibbonTask</code>s in the factory queue to the ribbon and returns
	 * the ribbon factory for additional modification or creation.
	 * <p>
	 * If no contextual ribbon tasks are in the queue, nothing occurs.
	 * <p>
	 * This is equivalent to
	 * <code>addContextualTaskGroup(title, Color.BLUE)</code>.
	 * 
	 * @param title
	 *            the title
	 * @param color
	 *            the hue color of the task group
	 * @return the ribbon factory
	 */
	public RibbonFactory addContextualTaskGroup(String title) {
		return addContextualTaskGroup(title, Color.BLUE);
	}

	/**
	 * Adds a {@link RibbonContextualTaskGroup} comprised of the
	 * <code>RibbonTask</code>s in the factory queue to the ribbon and returns
	 * the ribbon factory for additional modification or creation. The
	 * contextual ribbon task queue is cleared after adding the ribbon task
	 * group.
	 * <p>
	 * If no contextual ribbon tasks are in the queue, nothing occurs.
	 * 
	 * @see #clearContextualTaskGroupsQueue()
	 * @param title
	 *            the title
	 * @param color
	 *            the hue color of the task group
	 * @return the ribbon factory
	 */
	public RibbonFactory addContextualTaskGroup(String title, Color color) {
		if (contextualTasks.size() > 0) {
			RibbonTask[] _tasks = new RibbonTask[contextualTasks.size()];
			RibbonContextualTaskGroup group = new RibbonContextualTaskGroup(
					title, color, contextualTasks.toArray(_tasks));
			ribbon.addContextualTaskGroup(group);
			clearContextualTaskGroupsQueue();
		}
		return this;
	}

	/**
	 * Adds the <code>group</code> to the ribbon and returns the ribbon factory
	 * for additional modification or creation.
	 * 
	 * @see #getContextualTaskGroup(String, Color)
	 * @param group
	 *            the contextual ribbon group to add
	 * @return the ribbon factory
	 */
	public RibbonFactory addContextualTaskGroup(RibbonContextualTaskGroup group) {
		ribbon.addContextualTaskGroup(group);
		return this;
	}

	/**
	 * Returns the contextual ribbon task group comprised of the
	 * <code>RibbonTask</code>s in the factory queue. The queue is
	 * <em><b>not</b></em> cleared when this method is called.
	 * 
	 * @param title
	 *            the title
	 * @param color
	 *            the hue color of the task group
	 * @return the ribbon factory
	 */
	public RibbonContextualTaskGroup getContextualTaskGroup(String title,
			Color color) {
		RibbonContextualTaskGroup group = null;
		if (contextualTasks.size() > 0) {
			RibbonTask[] _tasks = new RibbonTask[contextualTasks.size()];
			group = new RibbonContextualTaskGroup(title, color,
					contextualTasks.toArray(_tasks));
		}
		return group;
	}

	/**
	 * Clears the factory queue of contextual ribbon tasks to be added to the
	 * next {@link RibbonContextualTaskGroup}.
	 */
	public void clearContextualTaskGroupsQueue() {
		contextualTasks.clear();
	}

	/**
	 * Adds the <code>task</code> to the queue of {@link RibbonTask}s to be
	 * added to a contextual ribbon task group and returns the ribbon factory
	 * for additional modification or creation.
	 * <p>
	 * Contextual <code>RibbonTask</code>s are not added to the ribbon until a
	 * {@link RibbonContextualTaskGroup} is created with the ribbon tasks. After
	 * adding all the ribbon tasks for a particular contextual task group use
	 * the {@link #addContextualTaskGroup(String, Color)} method.
	 * 
	 * @see #addContextualTaskGroup(String, Color)
	 * @see #clearRibbonBandsQueue()
	 * @param title
	 *            the title to display at the top of the ribbon
	 * @return the ribbon factory
	 */
	public RibbonFactory addContextualTask(String title) {
		JRibbonBand[] _bands = new JRibbonBand[bands.size()];
		RibbonTask task = new RibbonTask(title, bands.toArray(_bands));
		contextualTasks.add(task);
		// TODO
		clearRibbonBandsQueue();
		return this;
	}

	/**
	 * Adds the <code>task</code> to the queue of {@link RibbonTask}s to be
	 * added to a contextual ribbon task group and returns the ribbon factory
	 * for additional modification or creation.
	 * <p>
	 * Contextual <code>RibbonTask</code>s are not added to the ribbon until a
	 * {@link RibbonContextualTaskGroup} is created with the ribbon tasks. After
	 * adding all the ribbon tasks for a particular contextual task group use
	 * the {@link #addContextualTaskGroup(String, Color)} method.
	 * 
	 * @see #addContextualTaskGroup(String, Color)
	 * @param task
	 *            the ribbon task to be added to a contextual ribbon task group
	 * @return the ribbon factory
	 */
	public RibbonFactory addContextualTask(RibbonTask task) {
		contextualTasks.add(task);
		return this;
	}

	/**
	 * Adds the <code>item</code> to the ribbon application menu and returns the
	 * factory for additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>addFooterMenuItem(text, null, actionListener)</code>.
	 * 
	 * @see #addFooterMenuItem(String, ResizableIcon, ActionListener)
	 * @param text
	 *            the text of this menu entry (must not be <code>null</code>)
	 * @param actionListener
	 *            the action listener for this footer entry
	 * @return the ribbon factory
	 */
	public RibbonFactory addFooterMenuItem(String text,
			ActionListener actionListener) {
		return addFooterMenuItem(text, null, actionListener);
	}

	/**
	 * Adds the <code>item</code> to the ribbon application menu and returns the
	 * factory for additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>addFooterMenuItem("", icon, actionListener)</code>.
	 * 
	 * @see #addFooterMenuItem(String, ResizableIcon, ActionListener)
	 * @param icon
	 *            the icon of this menu entry (must not be <code>null</code>)
	 * @param actionListener
	 *            the action listener for this footer entry
	 * @return the ribbon factory
	 */
	public RibbonFactory addFooterMenuItem(ResizableIcon icon,
			ActionListener actionListener) {
		return addFooterMenuItem("", icon, actionListener);
	}

	/**
	 * Adds the <code>item</code> to the ribbon application menu and returns the
	 * factory for additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>addFooterMenuItem(text, icon, null)</code>.
	 * 
	 * @see #addFooterMenuItem(String, ResizableIcon, ActionListener)
	 * @param text
	 *            the text of this menu entry (must not be <code>null</code>)
	 * @param icon
	 *            the icon of this menu entry (must not be <code>null</code>)
	 * @return the ribbon factory
	 */
	public RibbonFactory addFooterMenuItem(String text, ResizableIcon icon) {
		return addFooterMenuItem(text, icon, null);
	}

	/**
	 * Adds the <code>item</code> to the ribbon application menu and returns the
	 * factory for additional modification or creation.
	 * <p>
	 * A new <code>RibbonApplicationMenuEntryFooter</code> is created with the
	 * given parameters and passed to the
	 * {@link #addFooterMenuItem(RibbonApplicationMenuEntryFooter)} method.
	 * 
	 * @see #addFooterMenuItem(RibbonApplicationMenuEntryFooter)
	 * @param text
	 *            the text of this menu entry (must not be <code>null</code>)
	 * @param icon
	 *            the icon of this menu entry (must not be <code>null</code>)
	 * @param actionListener
	 *            the action listener for this footer entry
	 * @return the ribbon factory
	 */
	public RibbonFactory addFooterMenuItem(String text, ResizableIcon icon,
			ActionListener actionListener) {
		RibbonApplicationMenuEntryFooter item = new RibbonApplicationMenuEntryFooter(
				icon, text, actionListener);
		return addFooterMenuItem(item);
	}

	/**
	 * Adds the <code>item</code> to the ribbon application menu and returns the
	 * factory for additional modification or creation.
	 * 
	 * @param item
	 *            the footer menu item
	 * @return the ribbon factory
	 */
	public RibbonFactory addFooterMenuItem(RibbonApplicationMenuEntryFooter item) {
		applicationMenu.addFooterEntry(item);
		return this;
	}

	/**
	 * Adds the <code>item</code> to the factory queue of
	 * {@link RibbonApplicationMenuEntrySecondary}s and returns the factory for
	 * additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * 
	 * <code>addSubMenuItem(text, Helpers.getResizableIconFromURL("res/images/blank.png"), null, CommandButtonKind.ACTION_ONLY)</code>.
	 * 
	 * @see #addSubMenuItem(String, ResizableIcon, ActionListener,
	 *      CommandButtonKind)
	 * @param text
	 *            the text of this menu entry (must not be <code>null</code>)
	 * @return the ribbon factory
	 */
	public RibbonFactory addSubMenuItem(String text) {
		return addSubMenuItem(text, getResizableIconFromResource(getClass()
				.getResource("/res/images/blank.png")), null,
				CommandButtonKind.ACTION_ONLY);
	}

	/**
	 * Adds the <code>item</code> to the factory queue of
	 * {@link RibbonApplicationMenuEntrySecondary}s and returns the factory for
	 * additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>addSubMenuItem(text, icon, null, CommandButtonKind.ACTION_ONLY)</code>.
	 * 
	 * @see #addSubMenuItem(String, ResizableIcon, ActionListener,
	 *      CommandButtonKind)
	 * @param text
	 *            the text of this menu entry (must not be <code>null</code>)
	 * @param icon
	 *            the icon of this menu entry (must not be <code>null</code>)
	 * @return the ribbon factory
	 */
	public RibbonFactory addSubMenuItem(String text, ResizableIcon icon) {
		return addSubMenuItem(text, icon, null, CommandButtonKind.ACTION_ONLY);
	}

	/**
	 * Adds the <code>item</code> to the factory queue of
	 * {@link RibbonApplicationMenuEntrySecondary}s and returns the factory for
	 * additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>addSubMenuItem(text, icon, actionListener, CommandButtonKind.ACTION_ONLY)</code>.
	 * 
	 * @see #addSubMenuItem(String, ResizableIcon, ActionListener,
	 *      CommandButtonKind)
	 * @param text
	 *            the text of this menu entry (must not be <code>null</code>)
	 * @param icon
	 *            the icon of this menu entry (must not be <code>null</code>)
	 * @param actionListener
	 *            main action listener for this menu entry. If the entry kind is
	 *            JCommandButton.CommandButtonKind.POPUP_ONLY, this listener
	 *            will be ignored.
	 * @return the ribbon factory
	 */
	public RibbonFactory addSubMenuItem(String text, ResizableIcon icon,
			ActionListener actionListener) {
		return addSubMenuItem(text, icon, actionListener,
				CommandButtonKind.ACTION_ONLY);
	}

	/**
	 * Adds the <code>item</code> to the factory queue of
	 * {@link RibbonApplicationMenuEntrySecondary}s and returns the factory for
	 * additional modification or creation.
	 * <p>
	 * A new <code>RibbonApplicationMenuEntrySecondary</code> is created with
	 * the given parameters and passed to the
	 * {@link #addSubMenuItem(RibbonApplicationMenuEntrySecondary)} method.
	 * 
	 * @see #addSubMenuItem(RibbonApplicationMenuEntrySecondary)
	 * @param text
	 *            the text of this menu entry (must not be <code>null</code>)
	 * @param icon
	 *            the icon of this menu entry (must not be <code>null</code>)
	 * @param actionListener
	 *            main action listener for this menu entry. If the entry kind is
	 *            JCommandButton.CommandButtonKind.POPUP_ONLY, this listener
	 *            will be ignored.
	 * @param kind
	 *            the kind of the command button that will represent this menu
	 *            entry. (must not be <code>null</code>)
	 * @return the ribbon factory
	 */
	public RibbonFactory addSubMenuItem(String text, ResizableIcon icon,
			ActionListener actionListener, CommandButtonKind kind) {
		RibbonApplicationMenuEntrySecondary item = new RibbonApplicationMenuEntrySecondary(
				icon, text, actionListener, kind);
		return addSubMenuItem(item);
	}

	/**
	 * Adds the <code>item</code> to the factory queue of
	 * {@link RibbonApplicationMenuEntrySecondary}s and returns the factory for
	 * additional modification or creation.
	 * <p>
	 * Sub menu items are not added to a primary menu item until a primary menu
	 * item is created. When a primary menu item is created with the factory
	 * methods the factory queue of
	 * <code>RibbonApplicationMenuEntrySecondary</code>s are emptied into the
	 * newly created <code>RibbonApplicationMenuEntryPrimary</code>.
	 * 
	 * @param item
	 *            the sub menu item
	 * @return the ribbon factory
	 */
	public RibbonFactory addSubMenuItem(RibbonApplicationMenuEntrySecondary item) {
		subMenuItems.add(item);
		return this;
	}

	/**
	 * Prepares the factory for the next sub menu to be added to the ribbon
	 * application menu. When
	 * {@link #addMenuItem(RibbonApplicationMenuEntryPrimary)} is executed, the
	 * {@link #nextSubMenuTitle} gets set to <code>null</code>.
	 * <p>
	 * This is equivalent to calling <code>newSubMenuGroup("")</code>.
	 * 
	 * @see #newSubMenuGroup(String)
	 * @return the ribbon factory
	 */
	public RibbonFactory newSubMenuGroup() {
		return newSubMenuGroup("");
	}

	/**
	 * Prepares the factory for the next sub menu to be added to the ribbon
	 * application menu. When
	 * {@link #addMenuItem(RibbonApplicationMenuEntryPrimary)} is executed, the
	 * {@link #nextSubMenuTitle} gets set to <code>null</code>.
	 * 
	 * @param title
	 *            the next sub menu title
	 * @return the ribbon factory
	 */
	public RibbonFactory newSubMenuGroup(String title) {
		nextSubMenuTitle = title;
		return this;
	}

	/**
	 * A convenience method for adding a blank menu item to the ribbon
	 * application menu.
	 * <p>
	 * This is equivalent to
	 * <code>addMenuItem("", null, null, CommandButtonKind.ACTION_ONLY)</code>.
	 * <p>
	 * This may be needed if there are more sub menu items than there are
	 * primary menu items in the application menu. Essentially, this is a
	 * "hot fix" around the main issue which may not be fixed for a while.
	 * <p>
	 * <em><b>TODO</b> put sub menu items in a <code>JSrollPane</code></em>
	 * 
	 * @return the ribbon factory
	 */
	public RibbonFactory addSpacerMenuItem() {
		return addMenuItem("", null, null, CommandButtonKind.ACTION_ONLY);
	}

	/**
	 * Adds the <code>item</code> to the ribbon application menu and returns the
	 * factory for additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * 
	 * <code>addMenuItem(text, Helpers.getResizableIconFromURL("res/images/blank.png"), null, CommandButtonKind.POPUP_ONLY)</code>.
	 * 
	 * @see #addMenuItem(String, ResizableIcon, ActionListener,
	 *      CommandButtonKind)
	 * @param text
	 *            the text of this menu entry (must not be <code>null</code>)
	 * @return the ribbon factory
	 */
	public RibbonFactory addMenuItem(String text) {
		return addMenuItem(
				text,
				getResizableIconFromResource(RibbonFactory.class
						.getClassLoader().getResource(
								"example/resources/blank.png")), null,
				CommandButtonKind.POPUP_ONLY);
	}

	/**
	 * Adds the <code>item</code> to the ribbon application menu and returns the
	 * factory for additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>addMenuItem(text, icon, null, CommandButtonKind.POPUP_ONLY)</code>.
	 * 
	 * @see #addMenuItem(String, ResizableIcon, ActionListener,
	 *      CommandButtonKind)
	 * @param text
	 *            the text of this menu entry (must not be <code>null</code>)
	 * @param icon
	 *            the icon of this menu entry (must not be <code>null</code>)
	 * @return the ribbon factory
	 */
	public RibbonFactory addMenuItem(String text, ResizableIcon icon) {
		return addMenuItem(text, icon, null, CommandButtonKind.POPUP_ONLY);
	}

	/**
	 * Adds the <code>item</code> to the ribbon application menu and returns the
	 * factory for additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>addMenuItem(text, icon, actionListener, CommandButtonKind.ACTION_ONLY)</code>.
	 * 
	 * @see #addMenuItem(String, ResizableIcon, ActionListener,
	 *      CommandButtonKind)
	 * @param text
	 *            the text of this menu entry (must not be <code>null</code>)
	 * @param icon
	 *            the icon of this menu entry (must not be <code>null</code>)
	 * @param actionListener
	 *            main action listener for this menu entry. If the entry kind is
	 *            JCommandButton.CommandButtonKind.POPUP_ONLY, this listener
	 *            will be ignored.
	 * @return the ribbon factory
	 */
	public RibbonFactory addMenuItem(String text, ResizableIcon icon,
			ActionListener actionListener) {
		return addMenuItem(text, icon, actionListener,
				CommandButtonKind.ACTION_ONLY);
	}

	/**
	 * Adds the <code>item</code> to the ribbon application menu and returns the
	 * factory for additional modification or creation.
	 * <p>
	 * A new <code>RibbonApplicationMenuEntryPrimary</code> is created with the
	 * given parameters and passed to the
	 * {@link #addMenuItem(RibbonApplicationMenuEntryPrimary)} method.
	 * 
	 * @param text
	 *            the text of this menu entry (must not be <code>null</code>)
	 * @param icon
	 *            the icon of this menu entry (must not be <code>null</code>)
	 * @param actionListener
	 *            main action listener for this menu entry. If the entry kind is
	 *            JCommandButton.CommandButtonKind.POPUP_ONLY, this listener
	 *            will be ignored.
	 * @param kind
	 *            the kind of the command button that will represent this menu
	 *            entry. (must not be <code>null</code>)
	 * @return the ribbon factory
	 */
	public RibbonFactory addMenuItem(String text, ResizableIcon icon,
			ActionListener actionListener, CommandButtonKind kind) {
		RibbonApplicationMenuEntryPrimary item = new RibbonApplicationMenuEntryPrimary(
				icon, text, actionListener, kind);
		return addMenuItem(item);
	}

	/**
	 * Adds the <code>item</code> to the ribbon application menu and returns the
	 * factory for additional modification or creation.
	 * <p>
	 * Due to limitations of {@link RibbonApplicationMenuEntryPrimary} we cannot
	 * dynamically add sub menu items to a primary menu item. Instead use the
	 * factory methods to add sub menu items to the factory queue and when a
	 * primary menu item is added the queue is emptied, putting all the
	 * {@link RibbonApplicationMenuEntrySecondary}s in the new task.
	 * <p>
	 * In order to create sub menu item(s) they must be added as a sub menu
	 * group. To do this use either {@link #newSubMenuGroup()} or
	 * {@link #newSubMenuGroup(String)} and proceed to add sub menu items.
	 * 
	 * @see #clearSubMenuItemsQueue()
	 * @param item
	 *            the menu item
	 * @return the ribbon factory
	 */
	public RibbonFactory addMenuItem(RibbonApplicationMenuEntryPrimary item) {
		if (nextSubMenuTitle != null) {
			RibbonApplicationMenuEntrySecondary[] subItems = new RibbonApplicationMenuEntrySecondary[subMenuItems
					.size()];
			item.addSecondaryMenuGroup(nextSubMenuTitle,
					subMenuItems.toArray(subItems));
		}
		applicationMenu.addMenuEntry(item);

		clearSubMenuItemsQueue();
		return this;
	}

	/**
	 * Returns the queue of sub menu items to be added to the next
	 * <code>RibbonApplicationMenuEntryPrimary</code>.
	 * 
	 * @return the container reference holding all the sub menu items
	 */
	public List<RibbonApplicationMenuEntrySecondary> getSubMenuItemsQueue() {
		return subMenuItems;
	}

	/**
	 * Clears the factory queue of sub menu items to be added to the next
	 * {@link RibbonApplicationMenuEntryPrimary}.
	 */
	public void clearSubMenuItemsQueue() {
		subMenuItems.clear();
	}

	/**
	 * Sets the ribbon application menu to a default
	 * {@link RibbonApplicationMenu} (blank icon) and returns the factory for
	 * additional modification or creation.
	 * 
	 * @return the ribbon factory
	 */
	public RibbonFactory withMenu() {
		if (ribbon.getApplicationMenu() == null) {
			ribbon.setApplicationMenu(applicationMenu);
		}
		return this;
	}

	/**
	 * Sets the ribbon application menu to a default
	 * {@link RibbonApplicationMenu} and returns the factory for additional
	 * modification or creation.
	 * 
	 * @param icon
	 *            the application icon
	 * @return the ribbon factory
	 */
	public RibbonFactory withMenu(ResizableIcon icon) {
		if (ribbon.getApplicationMenu() == null) {
			ribbon.setApplicationMenu(applicationMenu);
			// TODO add application icon
			// ribbon.setIcon(icon);
		}
		return this;
	}

	/**
	 * Sets the ribbon application help button to a default icon and returns the
	 * factory for additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>withHelp(Helpers.getResizableIconFromURL("res/images/help.png"), null)</code>.
	 * 
	 * @return the ribbon factory
	 */
	public RibbonFactory withHelp() {
		return withHelp(getResizableIconFromResource(RibbonFactory.class
				.getClassLoader().getResource("example/resources/help.png")),
				null);
	}

	/**
	 * Sets the ribbon application help button to the specified
	 * <code>icon</code> and returns the factory for additional modification or
	 * creation.
	 * <p>
	 * This is equivalent to calling <code>withHelp(icon, null)</code>.
	 * 
	 * @param icon
	 *            the help icon
	 * @return the ribbon factory
	 */
	public RibbonFactory withHelp(ResizableIcon icon) {
		return withHelp(icon, null);
	}

	/**
	 * Configures the ribbon application help button with a default help icon
	 * and the specified <code>actionListener</code> and returns the factory for
	 * additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>withHelp(Helpers.getResizableIconFromURL("res/images/help.png"), actionListener)</code>.
	 * 
	 * @param actionListener
	 *            the action listener to handle what to do when the help button
	 *            is clicked
	 * @return the ribbon factory
	 */
	public RibbonFactory withHelp(ActionListener actionListener) {
		return withHelp(
				getResizableIconFromResource(getClass().getResource(
						"/res/images/help.png")), actionListener);
	}

	/**
	 * Configures the ribbon application help button with the specified
	 * <code>icon</code> and <code>actionListener</code> and returns the factory
	 * for additional modification or creation.
	 * 
	 * @see JRibbon#configureHelp(ResizableIcon, ActionListener)
	 * @param icon
	 *            the help icon
	 * @param actionListener
	 *            the action listener to handle what to do when the help button
	 *            is clicked
	 * @return the ribbon factory
	 */
	public RibbonFactory withHelp(ResizableIcon icon,
			ActionListener actionListener) {
		ribbon.configureHelp(icon, null);
		return this;
	}

	/**
	 * Associates the last added {@link JCommandButton} with the
	 * <code>priority</code> and returns the factory for additional modification
	 * or creation.
	 * <p>
	 * This mapping will be used later when the button is added to its ribbon
	 * band.
	 * <p>
	 * <em><b>Note:</b> there are no checks performed so any exception that may 
	 * arise from {@link HashMap#put(Object, Object)} or {@link LinkedList#getLast()}
	 * will be thrown</em>
	 * 
	 * @see HashMap#put(Object, Object)
	 * @see LinkedList#getLast()
	 * @param priority
	 *            the ribbon band priority
	 * @return the ribbon factory
	 */
	public RibbonFactory hasPriority(RibbonElementPriority priority) {
		commandButtonsPriority.put(commandButtons.getLast(), priority);
		return this;
	}

	/**
	 * Adds the <code>button</code> to the factory queue of
	 * {@link JCommandButton}s and returns the factory for additional
	 * modification or creation.
	 * <p>
	 * This is equivalent to calling <code>addButton(name, null)</code>.
	 * 
	 * @see #addButton(String, ResizableIcon)
	 * @param name
	 *            button title (may contain any number of words)
	 * @return the ribbon factory
	 */
	public RibbonFactory addButton(String name) {
		return addButton(name, null);
	}

	/**
	 * Adds the <code>button</code> to the factory queue of
	 * {@link JCommandButton}s and returns the factory for additional
	 * modification or creation.
	 * <p>
	 * This is equivalent to calling <code>addButton("", icon)</code>.
	 * 
	 * @see #addButton(String, ResizableIcon)
	 * @param icon
	 *            button icon (may be <code>null</code>)
	 * @return the ribbon factory
	 */
	public RibbonFactory addButton(ResizableIcon icon) {
		return addButton("", icon);
	}

	/**
	 * Adds the <code>button</code> to the factory queue of
	 * {@link JCommandButton}s and returns the factory for additional
	 * modification or creation.
	 * <p>
	 * This is equivalent to calling <code>addButton(name, icon, null)</code>.
	 * 
	 * @see #addButton(String, ResizableIcon, ActionListener)
	 * @param name
	 *            button title (may contain any number of words)
	 * @param icon
	 *            button icon (may be <code>null</code>)
	 * @return the ribbon factory
	 */
	public RibbonFactory addButton(String name, ResizableIcon icon) {
		return addButton(name, icon, (ActionListener) null);
	}

	/**
	 * Adds the <code>button</code> to the factory queue of
	 * {@link JCommandButton}s and returns the factory for additional
	 * modification or creation.
	 * <p>
	 * A new <code>JCommandButton</code> is created with the given parameters
	 * and passed to the {@link #addButton(JCommandButton)} method. The
	 * <code>actionListener</code> is added to the button's action listeners if
	 * not <code>null</code>. The button is set not to be flat.
	 * 
	 * @see #addButton(JCommandButton)
	 * @see JCommandButton#setFlat(boolean)
	 * @param name
	 *            button title (may contain any number of words)
	 * @param icon
	 *            button icon (may be <code>null</code>)
	 * @param actionListener
	 *            the action listener
	 * @return the ribbon factory
	 */
	public RibbonFactory addButton(String name, ResizableIcon icon,
			ActionListener actionListener) {
		JCommandButton button = new JCommandButton(name, icon);
		button.setFlat(false);
		if (actionListener != null) {
			button.addActionListener(actionListener);
		}
		return addButton(button);
	}

	/**
	 * Adds the <code>button</code> to the factory queue of
	 * {@link JCommandButton}s and returns the factory for additional
	 * modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>addButton(name, icon, kind, null, null, null, false)</code>.
	 * 
	 * @see #addButton(String, ResizableIcon, ActionListener, CommandButtonKind,
	 *      RichTooltip, RichTooltip, PopupPanelCallback, boolean)
	 * @param name
	 *            button title (may contain any number of words)
	 * @param icon
	 *            button icon (may be <code>null</code>)
	 * @param actionListener
	 *            main action listener for this menu entry. If the entry kind is
	 *            JCommandButton.CommandButtonKind.POPUP_ONLY, this listener
	 *            will be ignored.
	 * @param kind
	 *            the command button kind
	 * @return the ribbon factory
	 */
	public RibbonFactory addButton(String name, ResizableIcon icon,
			ActionListener actionListener, CommandButtonKind kind) {
		return addButton(name, icon, actionListener, kind, null, null, null,
				false);
	}

	/**
	 * Adds the <code>button</code> to the factory queue of
	 * {@link JCommandButton}s and returns the factory for additional
	 * modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>addButton(name, icon, CommandButtonKind.ACTION_ONLY, null, null, null, isFlat)</code>.
	 * 
	 * @see #addButton(String, ResizableIcon, ActionListener, CommandButtonKind,
	 *      RichTooltip, RichTooltip, PopupPanelCallback, boolean)
	 * @param name
	 *            button title (may contain any number of words)
	 * @param icon
	 *            button icon (may be <code>null</code>)
	 * @param actionListener
	 *            main action listener for this menu entry. If the entry kind is
	 *            JCommandButton.CommandButtonKind.POPUP_ONLY, this listener
	 *            will be ignored.
	 * @param isFlat
	 *            <code>true</code> if flat, <code>false</code> if not flat
	 * @return the ribbon factory
	 */
	public RibbonFactory addButton(String name, ResizableIcon icon,
			ActionListener actionListener, boolean isFlat) {
		return addButton(name, icon, actionListener,
				CommandButtonKind.ACTION_ONLY, null, null, null, isFlat);
	}

	/**
	 * Adds the <code>button</code> to the factory queue of
	 * {@link JCommandButton}s and returns the factory for additional
	 * modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>addButton(name, icon, kind, null, null, null, isFlat)</code>.
	 * 
	 * @see #addButton(String, ResizableIcon, ActionListener, CommandButtonKind,
	 *      RichTooltip, RichTooltip, PopupPanelCallback, boolean)
	 * @param name
	 *            button title (may contain any number of words)
	 * @param icon
	 *            button icon (may be <code>null</code>)
	 * @param actionListener
	 *            main action listener for this menu entry. If the entry kind is
	 *            JCommandButton.CommandButtonKind.POPUP_ONLY, this listener
	 *            will be ignored.
	 * @param kind
	 *            the command button kind
	 * @param isFlat
	 *            <code>true</code> if flat, <code>false</code> if not flat
	 * @return the ribbon factory
	 */
	public RibbonFactory addButton(String name, ResizableIcon icon,
			ActionListener actionListener, CommandButtonKind kind,
			boolean isFlat) {
		return addButton(name, icon, actionListener, kind, null, null, null,
				isFlat);
	}

	/**
	 * Adds the <code>button</code> to the factory queue of
	 * {@link JCommandButton}s and returns the factory for additional
	 * modification or creation.
	 * <p>
	 * A new <code>JCommandButton</code> is created with the given parameters
	 * and passed to the {@link #addButton(JCommandButton)} method.
	 * 
	 * @see #addButton(JCommandButton)
	 * @param name
	 *            button title (may contain any number of words)
	 * @param icon
	 *            button icon (may be <code>null</code>)
	 * @param kind
	 *            the command button kind
	 * @param actionListener
	 *            main action listener for this menu entry. If the entry kind is
	 *            JCommandButton.CommandButtonKind.POPUP_ONLY, this listener
	 *            will be ignored.
	 * @param isFlat
	 *            <code>true</code> if flat, <code>false</code> if not flat
	 * @param actionTooltip
	 *            the rich tooltip for the action part of the button
	 * @param popupTooltip
	 *            the rich tooltip for the popup part of the button
	 * @param popupCallback
	 *            the popup callback to display the popup menu when the popup
	 *            part of the button is selected
	 * @return the ribbon factory
	 */
	public RibbonFactory addButton(String name, ResizableIcon icon,
			ActionListener actionListener, CommandButtonKind kind,
			RichTooltip actionTooltip, RichTooltip popupTooltip,
			PopupPanelCallback popupCallback, boolean isFlat) {
		JCommandButton button = new JCommandButton(name, icon);
		button.setFlat(isFlat);
		if (actionListener != null) {
			button.addActionListener(actionListener);
		}
		button.setCommandButtonKind(kind);
		button.setActionRichTooltip(actionTooltip);
		button.setPopupRichTooltip(popupTooltip);
		button.setPopupCallback(popupCallback);
		return addButton(button);
	}

	/**
	 * Adds the <code>button</code> to the factory queue of
	 * {@link JCommandButton}s and returns the factory for additional
	 * modification or creation.
	 * <p>
	 * Buttons are not added to the ribbon band until a band is created. When a
	 * band is created with the factory methods the factory queue of
	 * <code>JCommandButton</code>s are emptied into the newly created
	 * <code>JRibbonBand</code>.
	 * 
	 * @param button
	 *            the command button
	 * @return the ribbon factory
	 */
	public RibbonFactory addButton(JCommandButton button) {
		commandButtons.add(button);
		return this;
	}

	/**
	 * Returns the queue of buttons to be added to the next
	 * <code>JRibbonBand</code>.
	 * 
	 * @return the container reference holding all the buttons
	 */
	public List<JCommandButton> getButtonsQueue() {
		return commandButtons;
	}

	/**
	 * Clears the factory queue of buttons to be added to the next
	 * {@link JRibbonBand}.
	 */
	public void clearButtonsQueue() {
		commandButtons.clear();
	}

	/**
	 * Adds the <code>task</code> to the factory instance and returns the
	 * factory for additional modifications or creation.
	 * <p>
	 * Due to limitations of {@link RibbonTask} we cannot dynamically add bands
	 * to a task. Instead use the factory methods to add bands to the factory
	 * queue and when a task is added the queue is emptied, putting all the
	 * {@link JRibbonBand}s in the new task.
	 * 
	 * @see #addBand(JRibbonBand)
	 * @see #clearRibbonBandsQueue()
	 * @param name
	 *            the ribbon task name
	 * @return the ribbon factory
	 */
	public RibbonFactory addTask(String name) {
		JRibbonBand[] _bands = new JRibbonBand[bands.size()];
		RibbonTask task = new RibbonTask(name, bands.toArray(_bands));
		ribbon.addTask(task);
		// TODO
		clearRibbonBandsQueue();
		return this;
	}

	/**
	 * Adds the <code>band</code> to the factory queue of {@link JRibbonBand}s
	 * and returns the factory for additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>RibbonFactory.addBand("", icon, null)</code>.
	 * 
	 * @see #addBand(String, ResizableIcon, ActionListener)
	 * @param icon
	 *            associated icon (for collapsed state)
	 * @return the ribbon factory
	 */
	public RibbonFactory addBand(ResizableIcon icon) {
		return addBand("", icon, null);
	}

	/**
	 * Adds the <code>band</code> to the factory queue of {@link JRibbonBand}s
	 * and returns the factory for additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>RibbonFactory.addBand(title, null, null)</code>.
	 * 
	 * @see #addBand(String, ResizableIcon, ActionListener)
	 * @param title
	 *            band title
	 * @return the ribbon factory
	 */
	public RibbonFactory addBand(String title) {
		return addBand(title, null, null);
	}

	/**
	 * Adds the <code>band</code> to the factory queue of {@link JRibbonBand}s
	 * and returns the factory for additional modification or creation.
	 * <p>
	 * This is equivalent to calling
	 * <code>RibbonFactory.addBand(title, icon, null)</code>.
	 * 
	 * @see #addBand(String, ResizableIcon, ActionListener)
	 * @param title
	 *            band title
	 * @param icon
	 *            associated icon (for collapsed state)
	 * @return the ribbon factory
	 */
	public RibbonFactory addBand(String title, ResizableIcon icon) {
		return addBand(title, icon, null);
	}

	/**
	 * Adds the <code>band</code> to the factory queue of {@link JRibbonBand}s
	 * and returns the factory for additional modification or creation.
	 * <p>
	 * A new <code>JRibbonBand</code> is created with the given parameters and
	 * passed to the {@link #addBand(JRibbonBand)} method.
	 * 
	 * @see #addBand(JRibbonBand)
	 * @param title
	 *            band title
	 * @param icon
	 *            associated icon (for collapsed state)
	 * @param actionListener
	 *            expand action listener (can be null)
	 * @return the ribbon factory
	 */
	public RibbonFactory addBand(String title, ResizableIcon icon,
			ActionListener actionListener) {
		JRibbonBand band = new JRibbonBand(title, icon, actionListener);
		band.setResizePolicies(getDefaultPoliciesFor(band));
		return addBand(band);
	}

	/**
	 * Adds the <code>band</code> to the factory queue of {@link JRibbonBand}s
	 * and returns the factory for additional modification or creation.
	 * <p>
	 * Ribbon bands are not added to the ribbon until a task is created. When a
	 * task is created with the factory methods the factory queue of
	 * <code>JRibbonBand</code>s are emptied into the newly created
	 * <code>RibbonTask</code>.
	 * 
	 * @see #clearButtonsQueue()
	 * @param band
	 *            the ribbon band
	 * @return the ribbon factory
	 */
	public RibbonFactory addBand(JRibbonBand band) {
		for (JCommandButton b : commandButtons) {
			// set the custom priority if there is a mapping
			RibbonElementPriority priority = commandButtonsPriority.get(b);
			priority = priority != null ? priority : RibbonElementPriority.TOP;

			band.addCommandButton(b, priority);
		}
		bands.add(band);

		clearButtonsQueue();
		return this;
	}

	/**
	 * Returns the queue of ribbon bands to be added to the next
	 * <code>RibbonTask</code>.
	 * 
	 * @return the container reference holding all the <code>JRibbonBand</code>s
	 */
	public List<JRibbonBand> getRibbonBandsQueue() {
		return bands;
	}

	/**
	 * Clears the factory queue of ribbon bands to be added to the next
	 * {@link RibbonTask}.
	 */
	public void clearRibbonBandsQueue() {
		bands.clear();
	}

	/**
	 * Returns the ribbon that has been configured via this factory.
	 * 
	 * @return the ribbon
	 */
	public JRibbon getRibbon() {
		return ribbon;
	}

	/**
	 * Returns a default list of resize policies for the <code>band</code>. The
	 * following policies are what are used:
	 * <ol>
	 * <li>{@link CoreRibbonResizePolicies.Mirror}</li>
	 * <li>{@link CoreRibbonResizePolicies.Mid2Low}</li>
	 * </ol>
	 * 
	 * @param band
	 *            the ribbon band
	 * @return list of resize policies
	 */
    public List<RibbonBandResizePolicy> getDefaultPoliciesFor(JRibbonBand band) {
        RibbonBandResizePolicy r1 = new CoreRibbonResizePolicies.Mirror(band.getControlPanel());
        RibbonBandResizePolicy r2 = new CoreRibbonResizePolicies.Mid2Low(band.getControlPanel());
        return new Vector<RibbonBandResizePolicy>(Arrays.asList(r1, r2));
	}

	/**
	 * A wrapper helper function to the
	 * <code>{@link #getResizableIconFromResource(URL, Dimension)}</code>
	 * function.
	 * <p>
	 * This function simply calls
	 * <code>getResizableIconFromResource(resource, new
	 * Dimension(48,48))</code> .
	 * </p>
	 * 
	 * @param resource
	 *            the resource to retrieve
	 * @return a <code>ResizableIcon</code> object with the resource
	 */
	public static ResizableIcon getResizableIconFromResource(URL resource) {
		return getResizableIconFromResource(resource, new Dimension(48, 48));
	}

	/**
	 * A wrapper helper function to the
	 * <code>{@link #getResizableIconFromResource(URL, Dimension)}</code>
	 * function.
	 * <p>
	 * This function simply calls
	 * <code>getResizableIconFromResource(Helpers.class.getClassLoader()
				.getResource(resource), new Dimension(48,48))</code>.
	 * <p>
	 * The <code>resource</code> string should be the full package path of where
	 * the resource is located omitting a preceding / (e.g.
	 * "example/resources/blah.png" and <em>NOT</em>
	 * "/example/resources/blah.png").
	 * 
	 * @param resource
	 *            the resource to retrieve
	 * @return a <code>ResizableIcon</code> object with the resource
	 */
	public static ResizableIcon getResizableIconFromResource(String resource) {
		return getResizableIconFromResource(RibbonFactory.class
				.getClassLoader().getResource(resource), new Dimension(48, 48));
	}

	/**
	 * A helper function that returns a <code>ResizableIcon</code> after
	 * retrieving the resource given.
	 * 
	 * @param resource
	 *            the resource to retrieve
	 * @param size
	 *            the size of the returned icon
	 * @return a <code>ResizableIcon</code> object with the resource
	 */
	public static ResizableIcon getResizableIconFromResource(URL resource,
			Dimension size) {
		return ImageWrapperResizableIcon.getIcon(resource, size);
	}

}
