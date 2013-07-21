/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.appearance;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.gephi.appearance.spi.Category;
import org.gephi.appearance.spi.TransformerUI;
import org.openide.util.NbBundle;

/**
 *
 * @author mbastian
 */
public class AppearanceToolbar implements AppearanceUIModelListener {

    protected final AppearanceUIController controller;
    protected AppearanceUIModel model;
    //Toolbars
    private final CategoryToolbar categoryToolbar;
    private final TransformerToolbar transformerToolbar;

    public AppearanceToolbar(AppearanceUIController controller) {
        this.controller = controller;
        categoryToolbar = new CategoryToolbar();
        transformerToolbar = new TransformerToolbar();

        controller.addPropertyChangeListener(this);
    }

    public void setup(AppearanceUIModel model) {
        this.model = model;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                categoryToolbar.setup();
                categoryToolbar.refreshTransformers();
            }
        });
    }

    public JToolBar getCategoryToolbar() {
        return categoryToolbar;
    }

    public JToolBar getTransformerToolbar() {
        return transformerToolbar;
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
//        if (pce.getPropertyName().equals(AppearanceUIModelEvent.CURRENT_ELEMENT_TYPE)) {
//            refreshSelectedElmntGroup((String) pce.getNewValue());
//        }
//        if (pce.getPropertyName().equals(RankingUIModel.CURRENT_TRANSFORMER)
//                || pce.getPropertyName().equals(RankingUIModel.CURRENT_ELEMENT_TYPE)) {
//            refreshTransformers();
//        }
//        if (pce.getPropertyName().equalsIgnoreCase(RankingUIModel.START_AUTO_TRANSFORMER)
//                || pce.getPropertyName().equalsIgnoreCase(RankingUIModel.STOP_AUTO_TRANSFORMER)) {
//            refreshDecoratedIcons();
//        }
    }

    private class AbstractToolbar extends JToolBar {

        public AbstractToolbar() {
            setFloatable(false);
            setRollover(true);
            Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
            setBorder(b);
        }

        @Override
        public void setEnabled(final boolean enabled) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (Component c : getComponents()) {
                        c.setEnabled(enabled);
                    }
                }
            });
        }
    }

    private class CategoryToolbar extends AbstractToolbar {

        private final List<ButtonGroup> buttonGroups = new ArrayList<ButtonGroup>();

        public CategoryToolbar() {
            //Init components
            elementGroup = new javax.swing.ButtonGroup();
            for (final String elmtType : AppearanceUIController.ELEMENT_CLASSES) {

                JToggleButton btn = new JToggleButton();
                btn.setFocusPainted(false);
                String btnLabel = elmtType;
                try {
                    btnLabel = NbBundle.getMessage(AppearanceToolbar.class, "AppearanceToolbar." + elmtType + ".label");
                } catch (MissingResourceException e) {
                }
                btn.setText(btnLabel);
                btn.setEnabled(false);
                btn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        model.setSelectedElementClass(elmtType);
                    }
                });
                elementGroup.add(btn);
                add(btn);
            }
            box = new javax.swing.JLabel();

            addSeparator();

            box.setMaximumSize(new java.awt.Dimension(32767, 32767));
            add(box);
        }

        private void clear() {
            //Clear precent buttons
            for (ButtonGroup bg : buttonGroups) {
                for (Enumeration<AbstractButton> btns = bg.getElements(); btns.hasMoreElements();) {
                    AbstractButton btn = btns.nextElement();
                    remove(btn);
                }
            }
            buttonGroups.clear();
        }

        protected void setup() {
            clear();
            if (model != null) {
                //Add transformers buttons, separate them by element group
                for (String elmtType : AppearanceUIController.ELEMENT_CLASSES) {
                    ButtonGroup buttonGroup = new ButtonGroup();
                    for (final Category c : controller.getCategories(elmtType)) {
                        //Build button
                        Icon icon = c.getIcon();
//                        DecoratedIcon decoratedIcon = getDecoratedIcon(icon, t);
//                        JToggleButton btn = new JToggleButton(decoratedIcon);
                        JToggleButton btn = new JToggleButton(icon);
                        btn.setToolTipText(c.getName());
                        btn.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
//                                model.setCurrentTransformer(t);
                            }
                        });
                        btn.setName(c.getName());
                        btn.setFocusPainted(false);
                        buttonGroup.add(btn);
                        add(btn);
                    }

                    buttonGroups.add(buttonGroup);
                }
            } else {
                elementGroup.clearSelection();
            }
        }

        protected void refreshTransformers() {
            //Select the right transformer
            int index = 0;
            for (String elmtType : AppearanceUIController.ELEMENT_CLASSES) {
                ButtonGroup g = buttonGroups.get(index);
                boolean active = model == null ? false : model.getSelectedElementClass().equals(elmtType);
                g.clearSelection();
                TransformerUI t = model.getCurrentTransformerUI(elmtType);
                String selected = model == null ? "" : t.getDisplayName();
                for (Enumeration<AbstractButton> btns = g.getElements(); btns.hasMoreElements();) {
                    AbstractButton btn = btns.nextElement();
                    btn.setVisible(active);
                    if (btn.getName().equals(selected)) {
                        g.setSelected(btn.getModel(), true);
                    }
                }
                index++;
            }
        }

        protected void refreshSelectedElmntGroup() {
            String selected = model.getSelectedElementClass();
            ButtonModel buttonModel = null;
            Enumeration<AbstractButton> en = elementGroup.getElements();
            for (String elmtType : AppearanceUIController.ELEMENT_CLASSES) {
                if (elmtType.equals(selected)) {
                    buttonModel = en.nextElement().getModel();
                    break;
                }
                en.nextElement();
            }
            elementGroup.setSelected(buttonModel, true);
        }
        private javax.swing.JLabel box;
        private javax.swing.ButtonGroup elementGroup;
    }

    private class TransformerToolbar extends AbstractToolbar {
    }
//    private void refreshDecoratedIcons() {
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                int index = 0;
//                for (String elmtType : AppearanceUIController.ELEMENT_CLASSES) {
//                    ButtonGroup g = buttonGroups.get(index++);
//                    boolean active = model == null ? false : model.getCurrentElementType().equals(elmtType);
//                    if (active) {
//                        for (Enumeration<AbstractButton> btns = g.getElements(); btns.hasMoreElements();) {
//                            btns.nextElement().repaint();
//                        }
//                    }
//                }
//            }
//        });
//    }
//    private DecoratedIcon getDecoratedIcon(Icon icon, final Transformer transformer) {
//        Icon decoration = ImageUtilities.image2Icon(ImageUtilities.loadImage("org/gephi/desktop/ranking/resources/chain.png", false));
//        return new DecoratedIcon(icon, decoration, new DecoratedIcon.DecorationController() {
//            @Override
//            public boolean isDecorated() {
//                return model != null && model.isAutoTransformer(transformer);
//            }
//        });
//    }
}