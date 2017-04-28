import MaterialDateTimePicker from 'material-datetime-picker';
import _ from 'underscore';

$.material.init();
_.each(document.querySelectorAll("input[type=datetime]"), (input) => {
    const picker = new MaterialDateTimePicker()
        .on('submit', (val) => {
            input.value = val.format("DD/MM/YYYY");
        });
    input.addEventListener('focus', () => picker.open());
});
// $('input[type=datetime]').bootstrapMaterialDatePicker({ format : 'DD MMMM YYYY - HH:mm' });
