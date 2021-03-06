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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.Set;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.ClientPluginAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset.GenericDataSetViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.material.GenericMaterialViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer;

/**
 * {@link IClientPluginFactory} implementation for <i>Generic</i> technology.
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginFactory extends
        AbstractClientPluginFactory<IGenericClientServiceAsync>
{
    public ClientPluginFactory(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final IViewContext<IGenericClientServiceAsync> createViewContext(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new GenericViewContext(originalViewContext);
    }

    //
    // IClientPluginFactory
    //

    @SuppressWarnings("unchecked")
    public final <T extends EntityType, I extends IIdentifiable> IClientPlugin<T, I> createClientPlugin(
            EntityKind entityKind)
    {
        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new ExperimentClientPlugin();
        }
        if (EntityKind.SAMPLE.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new SampleClientPlugin();
        }
        if (EntityKind.MATERIAL.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new MaterialClientPlugin();
        }
        if (EntityKind.DATA_SET.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new DataSetClientPlugin();
        }

        throw new UnsupportedOperationException("IClientPlugin for entity kind '" + entityKind
                + "' not implemented yet.");
    }

    public final Set<String> getEntityTypeCodes(EntityKind entityKind)
    {
        throw new UnsupportedOperationException(
                "Generic plugin factory supports every sample type.");
    }

    private String getViewerTitle(final String entityKindDictKey, final IIdentifiable identifiable)
    {
        return AbstractViewer.getTitle(getViewContext(), entityKindDictKey, identifiable);
    }

    private String getEditorTitle(final String entityKindDictKey, final IIdentifiable identifiable)
    {
        return AbstractRegistrationForm.getEditTitle(getViewContext(), entityKindDictKey,
                identifiable);
    }

    //
    // Helper classes
    //

    private final class SampleClientPlugin implements IClientPlugin<SampleType, IIdentifiable>
    {

        //
        // IViewClientPlugin
        //

        public ITabItemFactory createEntityViewer(final IIdentifiable identifiable)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent sampleViewer =
                                GenericSampleViewer.create(getViewContext(), identifiable);
                        return DefaultTabItem.create(getViewerTitle(Dict.SAMPLE, identifiable),
                                sampleViewer, getViewContext(), false);
                    }

                    public String getId()
                    {
                        return GenericSampleViewer.createId(identifiable);
                    }
                };
        }

        public final DatabaseModificationAwareWidget createRegistrationForEntityType(
                final SampleType sampleType)
        {
            GenericSampleRegistrationForm form =
                    new GenericSampleRegistrationForm(getViewContext(), sampleType);
            return new DatabaseModificationAwareWidget(form, form);
        }

        public final Widget createBatchRegistrationForEntityType(final SampleType sampleType)
        {
            return new GenericSampleBatchRegistrationForm(getViewContext(), sampleType);
        }

        public ITabItemFactory createEntityEditor(final IIdentifiable identifiable)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericSampleEditForm.create(getViewContext(), identifiable);
                        String title = getEditorTitle(Dict.SAMPLE, identifiable);
                        return DefaultTabItem.create(title, component, getViewContext(), true);
                    }

                    public String getId()
                    {
                        return GenericSampleEditForm.createId(identifiable, EntityKind.SAMPLE);
                    }
                };
        }

    }

    private final class MaterialClientPlugin extends
            ClientPluginAdapter<MaterialType, IIdentifiable>
    {

        @Override
        public final Widget createBatchRegistrationForEntityType(final MaterialType materialType)
        {
            return new GenericMaterialBatchRegistrationForm(getViewContext(), materialType);
        }

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifiable identifiable)
        {
            final TechId techId = TechId.create(identifiable);
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent materialViewer =
                                GenericMaterialViewer.create(getViewContext(), techId);
                        return DefaultTabItem.create(getViewerTitle(Dict.MATERIAL, identifiable),
                                materialViewer, getViewContext(), false);
                    }

                    public String getId()
                    {
                        return GenericMaterialViewer.createId(techId);
                    }
                };
        }

        @Override
        public ITabItemFactory createEntityEditor(final IIdentifiable identifiable)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericMaterialEditForm
                                        .create(getViewContext(), identifiable, true);
                        String title = getEditorTitle(Dict.MATERIAL, identifiable);
                        return DefaultTabItem.create(title, component, getViewContext(), true);
                    }

                    public String getId()
                    {
                        return GenericMaterialEditForm.createId(identifiable, EntityKind.MATERIAL);
                    }
                };
        }
    }

    private final class ExperimentClientPlugin extends
            ClientPluginAdapter<ExperimentType, IIdentifiable>
    {

        //
        // IViewClientPlugin
        //

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifiable identifiable)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent experimentViewer =
                                GenericExperimentViewer.create(getViewContext(), identifiable);
                        return DefaultTabItem.create(getViewerTitle(Dict.EXPERIMENT, identifiable),
                                experimentViewer, getViewContext(), false);
                    }

                    public String getId()
                    {
                        return GenericExperimentViewer.createId(identifiable);
                    }
                };
        }

        @Override
        public DatabaseModificationAwareWidget createRegistrationForEntityType(
                ExperimentType entityType)
        {
            GenericExperimentRegistrationForm form =
                    new GenericExperimentRegistrationForm(getViewContext(), entityType);
            return new DatabaseModificationAwareWidget(form, form);
        }

        @Override
        public ITabItemFactory createEntityEditor(final IIdentifiable identifiable)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericExperimentEditForm.create(getViewContext(), identifiable);
                        String title = getEditorTitle(Dict.EXPERIMENT, identifiable);
                        return DefaultTabItem.create(title, component, getViewContext(), true);
                    }

                    public String getId()
                    {
                        return GenericDataSetEditForm.createId(identifiable, EntityKind.EXPERIMENT);
                    }
                };
        }
    }

    private final class DataSetClientPlugin extends ClientPluginAdapter<DataSetType, IIdentifiable>
    {

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifiable identifiable)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent dataSetViewer =
                                GenericDataSetViewer.create(getViewContext(), identifiable);
                        return DefaultTabItem.create(getViewerTitle(Dict.DATA_SET, identifiable),
                                dataSetViewer, getViewContext(), false);
                    }

                    public String getId()
                    {
                        return GenericDataSetViewer.createId(identifiable);
                    }
                };
        }

        @Override
        public ITabItemFactory createEntityEditor(final IIdentifiable identifiable)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericDataSetEditForm.create(getViewContext(), identifiable);
                        String title = getEditorTitle(Dict.DATA_SET, identifiable);
                        return DefaultTabItem.create(title, component, getViewContext(), true);
                    }

                    public String getId()
                    {
                        return GenericDataSetEditForm.createId(identifiable, EntityKind.DATA_SET);
                    }
                };
        }
    }

}
