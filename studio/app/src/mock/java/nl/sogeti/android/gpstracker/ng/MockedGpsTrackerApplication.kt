/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
 ** Copyright: (c) 2016 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.ng

import nl.sogeti.android.gpstracker.ng.base.BaseConfiguration
import nl.sogeti.android.gpstracker.ng.base.BaseConfiguration.appComponent
import nl.sogeti.android.gpstracker.ng.base.dagger.AppModule
import nl.sogeti.android.gpstracker.ng.dagger.DaggerMockAppComponent
import nl.sogeti.android.gpstracker.ng.dagger.MockSystemModule
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration.featureComponent
import nl.sogeti.android.gpstracker.ng.features.dagger.DaggerFeatureComponent
import nl.sogeti.android.gpstracker.ng.features.dagger.FeatureModule
import nl.sogeti.android.gpstracker.ng.features.dagger.VersionInfoModule
import nl.sogeti.android.gpstracker.service.dagger.DaggerMockServiceComponent
import nl.sogeti.android.gpstracker.service.dagger.MockServiceModule
import nl.sogeti.android.gpstracker.service.dagger.ServiceConfiguration.serviceComponent

class MockedGpsTrackerApplication : GpsTrackerApplication() {

    override fun setupModules() {
        appComponent = DaggerMockAppComponent.builder()
                .appModule(AppModule(this))
                .mockSystemModule(MockSystemModule())
                .build()
        serviceComponent = DaggerMockServiceComponent.builder()
                .mockServiceModule(MockServiceModule())
                .build()
        featureComponent = DaggerFeatureComponent.builder()
                .appComponent(BaseConfiguration.appComponent)
                .serviceComponent(serviceComponent)
                .versionInfoModule(VersionInfoModule(version(), gitHash(), buildNumber()))
                .featureModule(FeatureModule(this))
                .build()
    }
}
