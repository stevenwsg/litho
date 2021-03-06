/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.util.LongSparseArray;

import com.facebook.litho.testing.ComponentTestHelper;
import com.facebook.litho.testing.TestComponent;
import com.facebook.litho.testing.TestDrawableComponent;
import com.facebook.litho.testing.TestViewComponent;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.testing.util.InlineLayoutSpec;
import com.facebook.yoga.YogaAlign;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(ComponentsTestRunner.class)
public class MountStateRemountTest {
  private ComponentContext mContext;

  @Before
  public void setup() {
    mContext = new ComponentContext(RuntimeEnvironment.application);
  }

  @Test
  public void testRemountSameLayoutState() {
    final TestComponent component1 = TestDrawableComponent.create(mContext)
        .build();
    final TestComponent component2 = TestDrawableComponent.create(mContext)
        .build();
    final TestComponent component3 = TestDrawableComponent.create(mContext)
        .build();
    final TestComponent component4 = TestDrawableComponent.create(mContext)
        .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c).flexShrink(0).alignContent(YogaAlign.FLEX_START)
                .child(component1)
                .child(component2)
                .build();
          }
        });

    assertTrue(component1.isMounted());
    assertTrue(component2.isMounted());

    ComponentTestHelper.mountComponent(
        mContext,
        lithoView,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c).flexShrink(0).alignContent(YogaAlign.FLEX_START)
                .child(component3)
                .child(component4)
                .build();
          }
        });

    assertTrue(component1.isMounted());
    assertTrue(component2.isMounted());
    assertFalse(component3.isMounted());
    assertFalse(component4.isMounted());

    final MountState mountState = Whitebox.getInternalState(lithoView,"mMountState");
    final LongSparseArray<MountItem> indexToItemMap =
        Whitebox.getInternalState(mountState,"mIndexToItemMap");

    final List<Component> components = new ArrayList<>();
    for (int i = 0; i < indexToItemMap.size(); i++) {
      components.add(indexToItemMap.valueAt(i).getComponent());
    }

    assertFalse(containsRef(components, component1));
    assertFalse(containsRef(components, component2));
    assertTrue(containsRef(components, component3));
    assertTrue(containsRef(components, component4));
  }

  /**
   * There was a crash when mounting a drawing in place of a view. This test is here to make sure
   * this does not regress. To reproduce this crash the pools needed to be in a specific state
   * as view layout outputs and mount items were being re-used for drawables.
   */
  @Test
  public void testRemountDifferentMountType() throws IllegalAccessException, NoSuchFieldException {
    clearPool("sLayoutOutputPool");
    clearPool("sViewNodeInfoPool");

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return TestViewComponent.create(c).buildWithLayout();
          }
        });

    ComponentTestHelper.mountComponent(
        mContext,
        lithoView,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return TestDrawableComponent.create(c).buildWithLayout();
          }
        });
  }

  @Test
  public void testRemountNewLayoutState() {
    final TestComponent component1 = TestDrawableComponent.create(mContext)
        .unique()
        .build();
    final TestComponent component2 = TestDrawableComponent.create(mContext)
        .unique()
        .build();
    final TestComponent component3 = TestDrawableComponent.create(mContext)
        .unique()
        .build();
    final TestComponent component4 = TestDrawableComponent.create(mContext)
        .unique()
        .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c).flexShrink(0).alignContent(YogaAlign.FLEX_START)
                .child(component1)
                .child(component2)
                .build();
          }
        });

    assertTrue(component1.isMounted());
    assertTrue(component2.isMounted());

    ComponentTestHelper.mountComponent(
        mContext,
        lithoView,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c).flexShrink(0).alignContent(YogaAlign.FLEX_START)
                .child(component3)
                .child(component4)
                .build();
          }
        });

    assertFalse(component1.isMounted());
    assertFalse(component2.isMounted());
    assertTrue(component3.isMounted());
    assertTrue(component4.isMounted());
  }

  @Test
  public void testRemountPartiallyDifferentLayoutState() {
    final TestComponent component1 = TestDrawableComponent.create(mContext)
        .build();
    final TestComponent component2 = TestDrawableComponent.create(mContext)
        .build();
    final TestComponent component3 = TestDrawableComponent.create(mContext)
        .build();
    final TestComponent component4 = TestDrawableComponent.create(mContext)
        .build();

    final LithoView lithoView = ComponentTestHelper.mountComponent(
        mContext,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c).flexShrink(0).alignContent(YogaAlign.FLEX_START)
                .child(component1)
                .child(component2)
                .build();
          }
        });

    assertTrue(component1.isMounted());
    assertTrue(component2.isMounted());

    ComponentTestHelper.mountComponent(
        mContext,
        lithoView,
        new InlineLayoutSpec() {
          @Override
          protected ComponentLayout onCreateLayout(ComponentContext c) {
            return Column.create(c).flexShrink(0).alignContent(YogaAlign.FLEX_START)
                .child(component3)
                .child(
                    Column.create(c).flexShrink(0).alignContent(YogaAlign.FLEX_START)
                        .wrapInView()
                        .child(component4))
                .build();
          }
        });

    assertTrue(component1.isMounted());
    assertFalse(component2.isMounted());
    assertFalse(component3.isMounted());
    assertTrue(component4.isMounted());
  }

  private boolean containsRef(List<?> list, Object object) {
    for (Object o : list) {
      if (o == object) {
        return true;
      }
    }
    return false;
  }

  private static void clearPool(String name) {
    final RecyclePool<?> pool =
        Whitebox.getInternalState(ComponentsPools.class, name);

    while (pool.acquire() != null) {
      // Run.
    }
  }
}
