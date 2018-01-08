package com.reactnativenavigation.viewcontrollers;

import android.app.Activity;
import android.view.ViewGroup;

import com.reactnativenavigation.BaseTest;
import com.reactnativenavigation.mocks.TopTabLayoutMock;
import com.reactnativenavigation.parse.NavigationOptions;
import com.reactnativenavigation.viewcontrollers.ComponentViewController.IReactView;
import com.reactnativenavigation.viewcontrollers.toptabs.TopTabController;
import com.reactnativenavigation.viewcontrollers.toptabs.TopTabsAdapter;
import com.reactnativenavigation.viewcontrollers.toptabs.TopTabsController;
import com.reactnativenavigation.views.TopTabsLayout;
import com.reactnativenavigation.views.TopTabsLayoutCreator;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TopTabsViewControllerTest extends BaseTest {

    private static final int SIZE = 2;

    private TopTabsController uut;
    private List<TopTabLayoutMock> tabs = new ArrayList<>(SIZE);
    private List<TopTabController> tabControllers = new ArrayList<>(SIZE);
    private List<NavigationOptions> tabOptions = new ArrayList<>(SIZE);
    private NavigationOptions navigationOptions;
    private TopTabsLayout topTabsLayout;

    @Override
    public void beforeEach() {
        super.beforeEach();
        tabControllers.clear();
        tabs.clear();
        Activity activity = newActivity();
        createTabs(activity);
        navigationOptions = new NavigationOptions();
        topTabsLayout = spy(new TopTabsLayout(activity, tabControllers, new TopTabsAdapter(tabControllers)));

        TopTabsLayoutCreator layoutCreator = Mockito.mock(TopTabsLayoutCreator.class);
        Mockito.when(layoutCreator.create()).thenReturn(topTabsLayout);
        uut = new TopTabsController(activity, "componentId", tabControllers, layoutCreator, navigationOptions);
    }

    private void createTabs(Activity activity) {
        for (int i = 0; i < SIZE; i++) {
            TopTabLayoutMock reactView = spy(new TopTabLayoutMock(activity));
            tabs.add(reactView);
            tabOptions.add(new NavigationOptions());
            tabControllers.add(spy(new TopTabController(activity,
                    "idTab" + i,
                    "tab" + i,
                    (activity1, componentId, componentName) -> reactView,
                    tabOptions.get(i))
            ));
        }
    }

    @Test
    public void createsViewFromComponentViewCreator() throws Exception {
        uut.ensureViewIsCreated();
        for (int i = 0; i < SIZE; i++) {
            verify(tabControllers.get(i), times(1)).createView();
        }
    }

    @Test
    public void componentViewDestroyedOnDestroy() throws Exception {
        uut.ensureViewIsCreated();
        TopTabsLayout topTabs = (TopTabsLayout) uut.getView();
        for (int i = 0; i < SIZE; i++) {
            verify(tab(topTabs, i), times(0)).destroy();
        }
        uut.destroy();
        for (ViewController tabController : tabControllers) {
            verify(tabController, times(1)).destroy();
        }
    }

    @Test
    public void lifecycleMethodsSentWhenSelectedTabChanges() throws Exception {
        uut.ensureViewIsCreated();
        TopTabLayoutMock initialTab = tabs.get(0);
        TopTabLayoutMock selectedTab = tabs.get(1);
        uut.onViewAppeared();
        uut.switchToTab(1);
        verify(initialTab, times(1)).sendComponentStop();
        verify(selectedTab, times(1)).sendComponentStart();
        verify(selectedTab, times(0)).sendComponentStop();
    }

    @Test
    public void lifecycleMethodsSentWhenSelectedPreviouslySelectedTab() throws Exception {
        uut.ensureViewIsCreated();
        uut.onViewAppeared();
        uut.switchToTab(1);
        uut.switchToTab(0);
        verify(tabs.get(0), times(1)).sendComponentStop();
        verify(tabs.get(0), times(2)).sendComponentStart();
        verify(tabs.get(1), times(1)).sendComponentStart();
        verify(tabs.get(1), times(1)).sendComponentStop();
    }

    @Test
    public void setOptionsOfInitialTab() throws Exception {
        uut.ensureViewIsCreated();
        uut.onViewAppeared();
        verify(tabControllers.get(0), times(1)).applyOptions(tabOptions.get(0));
    }

    @Test
    public void setOptionsWhenTabChanges() throws Exception {
        uut.ensureViewIsCreated();
        uut.onViewAppeared();
        verify(tabControllers.get(0), times(1)).applyOptions(tabOptions.get(0));
        uut.switchToTab(1);
        verify(tabControllers.get(1), times(1)).applyOptions(tabOptions.get(1));
        uut.switchToTab(0);
        verify(tabControllers.get(0), times(2)).applyOptions(tabOptions.get(0));
    }

    @Test
    public void appliesOptionsOnLayoutWhenVisible() throws Exception {
        uut.ensureViewIsCreated();
        uut.onViewAppeared();
        verify(topTabsLayout, times(1)).applyOptions(navigationOptions);
    }

    private IReactView tab(TopTabsLayout topTabs, final int index) {
        return (IReactView) ((ViewGroup) topTabs.getViewPager().getChildAt(index)).getChildAt(0);
    }
}