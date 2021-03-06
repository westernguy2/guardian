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
package ai.rideos.android.interactors;

import ai.rideos.android.common.model.LocationAndHeading;
import ai.rideos.android.common.model.VehicleInfo;
import ai.rideos.android.common.model.VehicleInfo.ContactInfo;
import ai.rideos.android.model.VehicleDisplayRouteLeg;
import ai.rideos.android.model.VehicleRegistration;
import ai.rideos.android.model.VehicleStatus;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.util.List;

public interface DriverVehicleInteractor {
    Single<VehicleStatus> getVehicleStatus(final String vehicleId);

    Completable createVehicle(final String vehicleId, final String fleetId, final VehicleRegistration vehicleRegistration);

    Completable markVehicleReady(final String vehicleId);

    Completable markVehicleNotReady(final String vehicleId);

    Completable finishSteps(final String vehicleId, final String taskId, final List<String> stepIds);

    Completable rejectTrip(final String vehicleId, final String tripId);

    Completable cancelTrip(final String tripId);

    Completable updateVehicleLocation(final String vehicleId, final LocationAndHeading locationAndHeading);

    Completable updateVehicleRoute(final String vehicleId, final List<VehicleDisplayRouteLeg> updatedLegs);

    Completable updateContactInfo(final String vehicleId, final ContactInfo contactInfo);

    Completable updateLicensePlate(final String vehicleId, final String licensePlate);

    Single<VehicleInfo> getVehicleInfo(final String vehicleId);

    void shutDown();
}
