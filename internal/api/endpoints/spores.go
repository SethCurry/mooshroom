package endpoints

import (
	"fmt"
	"strconv"

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

	return req.JSONResponse(200, response)
}

type GetSporeDHTSensor struct {
	ID   int    `json:"id"`
	Name string `json:"name"`
	Pin  int    `json:"pin"`
}

type GetSporeResult struct {
	ID         int                  `json:"id"`
	Name       string               `json:"name"`
	DHTSensors []*GetSporeDHTSensor `json:"dht_sensors"`
}

func GetSpore(req *api.RequestContext) error {
	sporeIDStr := req.Params.ByName("spore_id")

	sporeID, err := strconv.Atoi(sporeIDStr)
	if err != nil {
		return fmt.Errorf("spore_id in URL is not a valid integer: %q: %w", sporeIDStr, err)
	}

	sporeResult, err := req.DB.Spores().GetByID(req.Context(), sporeID)
	if err != nil {
		return fmt.Errorf("failed to get spore %d: %w", sporeID, err)
	}

	dhtSensors := make([]*GetSporeDHTSensor, len(sporeResult.DHTSensors))
	for k, v := range sporeResult.DHTSensors {
		dhtSensors[k] = &GetSporeDHTSensor{
			ID:   v.ID,
			Name: v.Name,
			Pin:  v.Pin,
		}
	}

	return req.JSONResponse(200, &GetSporeResult{
		ID:         sporeResult.ID,
		Name:       sporeResult.Name,
		DHTSensors: dhtSensors,
	})
}
