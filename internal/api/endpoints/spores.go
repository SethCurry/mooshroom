package endpoints

import (
	"encoding/json"
	"fmt"

	"github.com/SethCurry/mooshroom/internal/api"
)

type ListSporesDHTSensor struct {
	ID   int    `json:"id"`
	Name string `json:"name"`
	Pin  int    `json:"gpio_pin"`
}

type ListSporesResponseItem struct {
	ID         int                    `json:"id"`
	Name       string                 `json:"name"`
	DHTSensors []*ListSporesDHTSensor `json:"dht_sensors"`
}

type ListSporesResponse struct {
	Spores []*ListSporesResponseItem `json:"spores"`
}

func ListSpores(req *api.RequestContext) error {
	spores, err := req.DB.Spores().Query(req.Context())
	if err != nil {
		return fmt.Errorf("failed while querying spores in DB: %w", err)
	}

	responseItems := make([]*ListSporesResponseItem, len(spores))
	for k, v := range spores {
		dhtSensorsResult, err := req.DB.DHTSensors().ListSensorsForSporeID(req.Context(), v.ID)
		if err != nil {
			return fmt.Errorf("failed to query DHT sensors for spore with ID %d: %w", v.ID, err)
		}

		dhtSensors := make([]*ListSporesDHTSensor, len(dhtSensorsResult))
		for i, j := range dhtSensorsResult {
			dhtSensors[i] = &ListSporesDHTSensor{
				ID:   j.ID,
				Name: j.Name,
				Pin:  j.Pin,
			}
		}

		responseItems[k] = &ListSporesResponseItem{
			ID:         v.ID,
			Name:       v.Name,
			DHTSensors: dhtSensors,
		}
	}

	response := &ListSporesResponse{
		Spores: responseItems,
	}

	marshalled, err := json.Marshal(response)
	if err != nil {
		return fmt.Errorf("failed to build JSON response for list of spores: %w", err)
	}

	req.Writer.WriteHeader(200)
	req.Writer.Write(marshalled)

	return nil
}
