/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.ng.dagger

import android.content.IntentFilter
import dagger.Module
import dagger.Provides
import nl.sogeti.android.gpstracker.integration.ServiceManagerInterface
import nl.sogeti.android.gpstracker.ng.util.MockServiceManager
import javax.inject.Named

@Module
class MockIntegrationModule {

    @Provides @Named("loggingStateFilter")
    fun loggingStateIntentFilter(): IntentFilter {
        return IntentFilter("nl.sogeti.android.gpstracker.ng.mock.broadcast")
    }

    @Provides
    fun serviceManagerInterface(): ServiceManagerInterface {
        return MockServiceManager()
    }

    @Provides @Named("providerAuthority")
    fun providerAuthority(): String {
        return "nl.sogeti.android.gpstracker.ng.mock.authority"
    }
}