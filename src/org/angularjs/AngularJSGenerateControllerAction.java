package org.angularjs;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.actions.GotoFileAction;
import com.intellij.ide.util.EditSourceUtil;
import com.intellij.ide.util.gotoByName.*;
import com.intellij.lang.Language;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.xml.XmlTokenImpl;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: joh.000
 * Date: 4/28/12
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class AngularJSGenerateControllerAction extends PsiElementBaseIntentionAction {
    public AngularJSGenerateControllerAction() {
        setText("Generate AngularJS Controller   .");
    }

    @Override
    public void invoke(@NotNull final Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        class GotoJsFiles extends GotoActionBase{

            @Override
            protected void gotoActionPerformed(AnActionEvent e) {
                assert project != null;
                if (DumbService.getInstance(project).isDumb()) {
                    DumbService.getInstance(project).showDumbModeNotification("Goto Class action is not available until indices are built, using Goto File instead");

                    myInAction = null;
                    new GotoFileAction().actionPerformed(e);
                    return;
                }

                PsiDocumentManager.getInstance(project).commitAllDocuments();

                FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.class");
                final GotoClassModel2 model = new GotoClassModel2(project);
                showNavigationPopup(e, model, new GotoActionBase.GotoActionCallback<Language>() {
                    @Override
                    protected ChooseByNameFilter<Language> createFilter(@NotNull ChooseByNamePopup popup) {
                        return new ChooseByNameLanguageFilter(popup, model, GotoClassSymbolConfiguration.getInstance(project), project);
                    }

                    @Override
                    public void elementChosen(ChooseByNamePopup popup, Object element) {
                        AccessToken token = ReadAction.start();
                        try {
                            if (element instanceof PsiElement) {
                                final PsiElement psiElement;// = getElement(((PsiElement) element), popup);
                                NavigationUtil.activateFileWithPsiElement(psiElement, !popup.isOpenInCurrentWindowRequested());
                            } else {
                                EditSourceUtil.navigate(((NavigationItem) element), true, popup.isOpenInCurrentWindowRequested());
                            }
                        } finally {
                            token.finish();
                        }
                    }
                }, "Classes matching pattern", true);

            }
        }


    }

    @Override

    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        try {
            return (element instanceof XmlTokenImpl) && (element.getParent().getPrevSibling().getPrevSibling().getText().equals("ng-controller"));
        } catch (Exception e) {
            return false;
        }
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return getText();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
