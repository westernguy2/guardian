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
package ai.rideos.android.driver_app.online.waiting_for_pickup;

import ai.rideos.android.common.authentication.User;
import ai.rideos.android.common.device.DeviceLocator;
import ai.rideos.android.common.model.LocationAndHeading;
import ai.rideos.android.common.model.map.CameraUpdate;
import ai.rideos.android.common.model.map.CenterPin;
import ai.rideos.android.common.model.map.DrawableMarker;
import ai.rideos.android.common.model.map.DrawablePath;
import ai.rideos.android.common.model.map.LatLngBounds;
import ai.rideos.android.common.model.map.MapSettings;
import ai.rideos.android.common.reactive.SchedulerProvider;
import ai.rideos.android.common.reactive.SchedulerProviders.DefaultSchedulerProvider;
import ai.rideos.android.common.utils.Markers;
import ai.rideos.android.common.utils.Paths;
import ai.rideos.android.common.view.resources.ResourceProvider;
import ai.rideos.android.common.viewmodel.progress.ProgressSubject;
import ai.rideos.android.common.viewmodel.progress.ProgressSubject.ProgressState;
import ai.rideos.android.driver_app.R;
import ai.rideos.android.interactors.DriverVehicleInteractor;
import ai.rideos.android.model.TripResourceInfo;
import ai.rideos.android.model.VehiclePlan.Waypoint;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.BehaviorSubject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DefaultWaitingForPickupViewModel implements WaitingForPickupViewModel {
    private static final int POLL_INTERVAL_MILLIS = 1000;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Waypoint waypoint;
    private final DriverVehicleInteractor vehicleInteractor;
    private final User user;
    private final ResourceProvider resourceProvider;
    private final SchedulerProvider schedulerProvider;
    private final BehaviorSubject<LocationAndHeading> currentLocation = BehaviorSubject.create();
    private final ProgressSubject progressSubject = new ProgressSubject();

    public DefaultWaitingForPickupViewModel(final DriverVehicleInteractor vehicleInteractor,
                                            final User user,
                                            final Waypoint waypoint,
                                            final ResourceProvider resourceProvider,
                                            final DeviceLocator deviceLocator) {
        this(vehicleInteractor, user, waypoint, resourceProvider, deviceLocator, new DefaultSchedulerProvider());
    }

    public DefaultWaitingForPickupViewModel(final DriverVehicleInteractor vehicleInteractor,
                                            final User user,
                                            final Waypoint waypoint,
                                            final ResourceProvider resourceProvider,
                                            final DeviceLocator deviceLocator,
                                            final SchedulerProvider schedulerProvider) {
        this.waypoint = waypoint;
        this.vehicleInteractor = vehicleInteractor;
        this.user = user;
        this.resourceProvider = resourceProvider;
        this.schedulerProvider = schedulerProvider;
        compositeDisposable.add(
            deviceLocator.observeCurrentLocation(POLL_INTERVAL_MILLIS).subscribe(currentLocation::onNext)
        );
    }

    @Override
    public String getPassengersToPickupText() {
        final String passengersToPickupText;
        final TripResourceInfo tripResourceInfo = waypoint.getAction().getTripResourceInfo();
        if (tripResourceInfo.getNumPassengers() > 1) {
            final int numberOfRidersExcludingRequester = tripResourceInfo.getNumPassengers() - 1;
            passengersToPickupText = String.format(
                "%s + %s",
                tripResourceInfo.getNameOfTripRequester(),
                numberOfRidersExcludingRequester
            );
        } else {
            passengersToPickupText = tripResourceInfo.getNameOfTripRequester();
        }

        return resourceProvider.getString(R.string.waiting_for_pickup_title_format, passengersToPickupText);
    }

    @Override
    public void confirmPickup() {
        compositeDisposable.add(
            progressSubject.followAsyncOperation(
                vehicleInteractor.finishSteps(
                    user.getId(),
                    waypoint.getTaskId(),
                    waypoint.getStepIds()
                )
            )
        );
    }

    @Override
    public Observable<ProgressState> getConfirmingPickupProgress() {
        return progressSubject.observeProgress();
    }

    @Override
    public Observable<MapSettings> getMapSettings() {
        return Observable.just(new MapSettings(false, CenterPin.hidden()));
    }

    @Override
    public Observable<CameraUpdate> getCameraUpdates() {
        return currentLocation.observeOn(schedulerProvider.computation())
            .map(location -> {
                final LatLngBounds bounds = Paths.getBoundsForPath(Arrays.asList(
                    location.getLatLng(),
                    waypoint.getAction().getDestination()
                ));
                return CameraUpdate.fitToBounds(bounds);
            });
    }

    @Override
    public Observable<Map<String, DrawableMarker>> getMarkers() {
        return currentLocation.observeOn(schedulerProvider.computation())
            .map(location -> {
                final Map<String, DrawableMarker> markers = new HashMap<>();
                markers.put(
                    Markers.PICKUP_MARKER_KEY,
                    Markers.getPickupMarker(waypoint.getAction().getDestination(), resourceProvider)
                );
                markers.put(
                    Markers.VEHICLE_KEY,
                    Markers.getVehicleMarker(location.getLatLng(), location.getHeading(), resourceProvider)
                );
                return markers;
            });
    }

    @Override
    public Observable<List<DrawablePath>> getPaths() {
        return Observable.just(Collections.emptyList());
    }

    @Override
    public void destroy() {
        compositeDisposable.dispose();
    }
}
