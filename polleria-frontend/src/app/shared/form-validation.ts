import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export const textoObligatorio: ValidatorFn = (control) =>
  typeof control.value === 'string' && control.value.trim().length ? null : { required: true };

// Comprueba el formato decimal sin calcular importes ni redondear valores.
export const precioMonetario: ValidatorFn = (control) => {
  const value = String(control.value ?? '');
  return /^(?:0|[1-9]\d{0,7})(?:\.\d{1,2})?$/.test(value) && Number(value) > 0
    ? null
    : { precio: true };
};

export function mensajeValidacion(control: AbstractControl): string {
  if (!control.touched || !control.errors) return '';
  const errors: ValidationErrors = control.errors;
  if (errors['required']) return 'Este campo es obligatorio.';
  if (errors['maxlength']) return `Máximo ${errors['maxlength'].requiredLength} caracteres.`;
  if (errors['precio']) return 'Ingresa un precio mayor que cero, hasta 8 enteros y 2 decimales.';
  if (errors['email']) return 'Ingresa un correo válido.';
  if (errors['min']) return 'Selecciona una categoría.';
  return 'Revisa el formato de este campo.';
}
