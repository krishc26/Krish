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

package ch.systemsx.cisd.openbis.generic.shared.authorization.validator;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * A {@link IValidator} implementation suitable for {@link Project}.
 * 
 * @author Izabela Adamczyk
 */
public final class ProjectValidator extends AbstractValidator<Project>
{
    private final IValidator<Group> groupValidator;

    public ProjectValidator()
    {
        groupValidator = new GroupValidator();
    }

    //
    // AbstractValidator
    //

    @Override
    public final boolean doValidation(final PersonPE person, final Project value)
    {
        final Group group = value.getGroup();
        return groupValidator.isValid(person, group);
    }
    
}
