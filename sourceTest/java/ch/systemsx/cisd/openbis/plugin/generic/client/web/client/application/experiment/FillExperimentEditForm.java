/*
 * Copyright 2008 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentTypeSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ProjectSelectionWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.PropertyField;

/**
 * A {@link AbstractDefaultTestCommand} extension for editing experiment.
 * 
 * @author Izabela Adamczyk
 */
public final class FillExperimentEditForm extends AbstractDefaultTestCommand
{

    private final TechId experimentId;

    private final List<PropertyField> properties;

    private String newProjectOrNull;

    private String samplesOrNull;

    public FillExperimentEditForm()
    {
        this(TechId.createWildcardTechId());
    }

    private FillExperimentEditForm(final TechId experimentId)
    {
        this.experimentId = experimentId;
        this.properties = new ArrayList<PropertyField>();
        addCallbackClass(GenericExperimentEditForm.ExperimentInfoCallback.class);
        addCallbackClass(GenericExperimentEditForm.ListSamplesCallback.class);
        addCallbackClass(ProjectSelectionWidget.ListProjectsCallback.class);
        addCallbackClass(ExperimentTypeSelectionWidget.ListItemsCallback.class);
    }

    public final FillExperimentEditForm addProperty(final PropertyField property)
    {
        assert property != null : "Unspecified property";
        properties.add(property);
        return this;
    }

    public final FillExperimentEditForm withSamples(final String allSamples)
    {
        this.samplesOrNull = allSamples;
        return this;
    }

    public final void execute()
    {
        String simpleId = "generic-experiment-edit_" + experimentId + "_form";
        String id = GenericExperimentEditForm.ID_PREFIX + simpleId;
        for (final PropertyField property : properties)
        {
            final Widget widget = GWTTestUtil.getWidgetWithID(id + property.getPropertyFieldId());
            if (widget instanceof Field<?>)
            {
                ((Field<?>) widget).setRawValue(property.getPropertyFieldValue());
            } else
            {
                throw new IllegalStateException("Wrong widget type");
            }
        }
        if (newProjectOrNull != null)
        {
            final ProjectSelectionWidget projectSelector =
                    (ProjectSelectionWidget) GWTTestUtil.getWidgetWithID(ProjectSelectionWidget.ID
                            + ProjectSelectionWidget.SUFFIX + simpleId);
            GWTUtils.setSelectedItem(projectSelector, ModelDataPropertyNames.PROJECT_IDENTIFIER,
                    newProjectOrNull);
        }
        if (samplesOrNull != null)
        {
            final TextArea samplesField =
                    (TextArea) GWTTestUtil
                            .getWidgetWithID(ExperimentSamplesArea.createId(simpleId));
            samplesField.setRawValue(samplesOrNull);
        }
        GWTTestUtil.clickButtonWithID(id + AbstractRegistrationForm.SAVE_BUTTON);
    }

    public FillExperimentEditForm changeProject(String newProject)
    {
        this.newProjectOrNull = newProject;
        return this;
    }
}
