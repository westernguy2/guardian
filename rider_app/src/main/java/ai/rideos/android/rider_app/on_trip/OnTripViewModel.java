/**
 * Copyright 2018-2019 rideOS, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.rideos.android.rider_app.on_trip;

import ai.rideos.android.common.viewmodel.BackListener;
import ai.rideos.android.common.viewmodel.ViewModel;
import ai.rideos.android.model.OnTripDisplayState;
import ai.rideos.android.rider_app.on_trip.confirming_cancel.ConfirmingCancelListener;
import ai.rideos.android.rider_app.on_trip.confirming_edit_pickup.ConfirmingEditPickupListener;
import ai.rideos.android.rider_app.on_trip.current_trip.CurrentTripListener;
import ai.rideos.android.rider_app.pre_trip.confirm_location.ConfirmLocationListener;
import io.reactivex.Observable;

public interface OnTripViewModel extends CurrentTripListener, ConfirmLocationListener, BackListener, ViewModel,
    ConfirmingCancelListener, ConfirmingEditPickupListener {
    void initialize(final String tripId);

    Observable<OnTripDisplayState> getDisplayState();
}
