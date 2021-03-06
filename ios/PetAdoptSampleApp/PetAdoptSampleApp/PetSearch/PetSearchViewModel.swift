//
//  PetSearchViewModel.swift
//  PetAdoptSampleApp
//
//  Created by laurenyew on 2/17/20.
//  Copyright © 2020 laurenyew. All rights reserved.
//

import SwiftUI
import Combine

class PetSearchViewModel: ObservableObject, Identifiable {
    @Published var location: String = ""
    
    @Published var dataSource: [AnimalRowViewModel] = []
    
    private let PetAdoptSearchAPI: PetAdoptSearchAPI
    
    private var disposables = Set<AnyCancellable>()
    
    init(PetAdoptSearchAPI: PetAdoptSearchAPI,
         scheduler: DispatchQueue = DispatchQueue(label: "PetSearchViewModel")) {
        self.PetAdoptSearchAPI = PetAdoptSearchAPI
        $location
        .dropFirst(1)
        .debounce(for: .seconds(0.5), scheduler: scheduler)
        .sink(receiveValue: searchForNearbyDogs(forLocation:))
        .store(in: &disposables)
    }
    
    func searchForNearbyDogs(forLocation location:String) {
        PetAdoptSearchAPI.getDogsNearMe(forLocation: location)
        .map { response in
                response.animals.map(AnimalRowViewModel.init)
        }
        .receive(on: DispatchQueue.main)
        .sink(receiveCompletion: { [weak self] value in
            guard let self = self else { return }
            switch value {
            case .failure:
                self.dataSource = [] // Clear out data on failure
            case .finished:
                break
            }
        }) { [weak self] animalViewModels in
            guard let self = self else { return }
            self.dataSource = animalViewModels// Success: Update data source
        }
        .store(in: &disposables)
    }
}
