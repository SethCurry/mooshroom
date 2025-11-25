package validate

import (
	"fmt"
	"strings"
)

func NewValidationError(field, message string) *ValidationError {
	return &ValidationError{
		Field:   field,
		Message: message,
	}
}

type ValidationError struct {
	Field   string
	Message string
}

func (e *ValidationError) PrependField(field string) *ValidationError {
	e.Field = field + "." + e.Field
	return e
}

func (e *ValidationError) AppendField(field string) *ValidationError {
	e.Field = e.Field + "." + field
	return e
}

func (e *ValidationError) Error() string {
	return fmt.Sprintf("validation error in %q: %s", e.Field, e.Message)
}

type MultiValidationError struct {
	Errors []*ValidationError
}

func (e *MultiValidationError) AddError(error *ValidationError) {
	e.Errors = append(e.Errors, error)
}

func (e *MultiValidationError) Error() string {
	errors := make([]string, len(e.Errors))
	for i, err := range e.Errors {
		errors[i] = "\t" + err.Error()
	}
	errorStrings := strings.Join(errors, "\n")

	return fmt.Sprintf("validation errors:\n%s", errorStrings)
}

type Validator interface {
	Validate() []*ValidationError
}

func Validate(v Validator) error {
	errors := v.Validate()
	if len(errors) == 0 {
		return nil
	}

	return &MultiValidationError{
		Errors: errors,
	}
}
