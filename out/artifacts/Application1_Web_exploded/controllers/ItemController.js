let itemDatabase = [];

loadItemDatabase();

// -------------------------------------- Validations & Form Control --------------------------------------

$('#save-item input').on('input change', realTimeValidate);
$('#update-item input').on('input change', realTimeValidate);

$('#txt-search-valuei').on('input change', function () {
    const value = $(this).val();
    const option = $('#search-item-by').val();

    const patterns = {
        'Code': /^I[0-9]{3,}$/,
        'Name': /^[a-zA-Z0-9\s\-']{1,50}$/
    };

    if (patterns[option]) {
        realTimeValidateInput(value, patterns[option], this);
    }
});

$('#search-item-by').on('change', function () {
    const value = $('#txt-search-valuei').val();
    const option = $(this).val();

    const patterns = {
        'Code': /^I[0-9]{3,}$/,
        'Name': /^[a-zA-Z0-9\s\-']{1,50}$/
    };

    if (patterns[option]) {
        realTimeValidateInput(value, patterns[option], '#txt-search-valuei');
    }
});

$('#close-savei-btn, #close-savei-icon').on('click', function () { 
    resetForm('#save-item', '#save-item input');
    initializeNextItemCode();
});
$('#close-updatei-btn, #close-updatei-icon').on('click', function () { 
    resetForm('#update-item', '#update-item input');
});

// -------------------------------------- Other Functions --------------------------------------

function loadItemDatabase() {
    $.ajax({
        url: "http://localhost:8080/Application1_Web_exploded/item",
        success: function (response) {
            itemDatabase = response;

            initializeNextItemCode();
            initializeOrderComboBoxes();
            loadAllItems();
            loadItemCount();
        },
        error: function (error) {
            console.log(error)
        }
    })
}

function initializeNextItemCode() {
    const prevCode = itemDatabase.length > 0 ? itemDatabase[itemDatabase.length - 1].itemCode : 'I000';
    const nextCode = generateNextID(prevCode);
    $('#txt-save-icode').val(nextCode);
    $('#txt-save-icode').removeClass('is-invalid').addClass('is-valid');
}

function getItemByName(name) {
    return itemDatabase.filter(i => i.description.toLowerCase() === name.toLowerCase());
}

function appendToItemTable(item) {
    $('#item-tbody').append(`
        <tr>
            <td>${item.itemCode}</td>
            <td>${item.description}</td>
            <td>${item.qtyOnHand}</td>
            <td>${parseFloat(item.unitPrice).toFixed(2)}</td>
        </tr>
    `);
}

function getItemByOption(option, value) {
    if (option === 'Code') return getItemByCode(value);
    if (option === 'Name') return getItemByName(value);
    return null;
}

function loadAllItems() {
    $('#item-tbody').empty();
    for (const item of itemDatabase) {
        appendToItemTable(item);
    }
}

function sortItemDatabaseByCode() {
    itemDatabase.sort(function(a, b) {
        const codeA = parseInt(a.itemCode.replace('I', ''), 10);
        const codeB = parseInt(b.itemCode.replace('I', ''), 10);
        return codeA - codeB;
    });
}

// -------------------------------------- CRUD Operations --------------------------------------

// Save Item
$('#save-item').on('submit', function (event) {
    event.preventDefault();
    let isValidated = $('#save-item input').toArray().every(element => $(element).hasClass('is-valid'));

    if (isValidated) {
        const itemCode = $('#txt-save-icode').val();
        const description = $('#txt-save-iname').val();
        const quantity = $('#txt-save-iqty').val();
        const unitPrice = $('#txt-save-iprice').val();

        if (!itemDatabase.some(i => i.itemCode === itemCode)) {
            $.ajax({
                url: "http://localhost:8080/Application1_Web_exploded/item",
                method: "POST",
                data: {
                    itemCode: itemCode,
                    description: description,
                    qtyOnHand: quantity,
                    unitPrice: unitPrice
                },

                success: function (response) {
                    showToast('success', 'Item saved successfully !');
                    resetForm('#save-item', '#save-item input');
                    loadItemDatabase();
                    sortItemDatabaseByCode();
                    loadItemCount();
                    initializeOrderComboBoxes();
                },
                error: function (error) {
                    console.log(error)
                }
            });
        } else {
            showToast('error', 'Item code already exists !');
        }
    }
});

// Update Item (Load Item Details)
$('#txt-update-icode').on('input', function (event) {
    if (itemDatabase.some(i => i.itemCode === $(this).val())) {
        const item = getItemByCode($(this).val());
        $('#txt-update-iname').val(item.description);
        $('#txt-update-iqty').val(item.qtyOnHand);
        $('#txt-update-iprice').val(item.unitPrice);
        $('#update-item input').addClass('is-valid').removeClass('is-invalid');
    } else {
        $('#txt-update-iname').val('');
        $('#txt-update-iqty').val('');
        $('#txt-update-iprice').val('');
        $('#update-item input').removeClass('is-valid');
    }
});

// Update Item
$('#update-item').on('submit', function (event) {
    event.preventDefault();

    let isValidated = $('#update-item input').toArray().every(element => $(element).hasClass('is-valid'));

    if (isValidated) {
        const itemCode = $('#txt-update-icode').val();
        const description = $('#txt-update-iname').val();
        const quantity = $('#txt-update-iqty').val();
        const unitPrice = $('#txt-update-iprice').val();

        if (itemDatabase.some(i => i.itemCode === itemCode)) {
            $.ajax({
                url: "http://localhost:8080/Application1_Web_exploded/item?itemCode="+ itemCode + "&description=" + description + "&qtyOnHand=" + quantity + "&unitPrice=" + unitPrice,
                method: "PUT",
                success: function (response) {
                    showToast('success', 'Item updated successfully !');
                    resetForm('#update-item', '#update-item input');
                    loadItemDatabase();
                },
                error: function (error) {
                    console.log(error)
                }
            });
        } else {
            showToast('error', 'Item code not found !');
        }
    }
});

// Search Item
$('#search-item').on('submit', function (event) {
    event.preventDefault();

    let isValidated = $('#txt-search-valuei').hasClass('is-valid');

    if (isValidated) {
        const value = $('#txt-search-valuei').val();
        const option = $('#search-item-by').val();

        $('#item-tbody').empty();
        
        const items = getItemByOption(option, value);

        if (Array.isArray(items) && items.length > 0) {
            for (const item of items) {
                appendToItemTable(item);
            }
            showToast('success', 'Item search completed successfully !');
        } else if (items && !Array.isArray(items)) {
            appendToItemTable(items);
            showToast('success', 'Item search completed successfully !');
        } else {
            showToast('error', `Item ${option} not found !`);
        }
    }
});

// Clear Item
$('#clear-item-btn').on('click', function () {
    $('#txt-search-valuei').val('');
    $('#txt-search-valuei').removeClass('is-invalid is-valid');
    $('#txt-search-valuei').next().hide();
    $('#search-item-by').val('Code');
    $('#item-tbody').empty();
    showToast('success', 'Item page cleared !');
});

// Delete Item
$('#delete-item-btn').on('click', function () {
    const value = $('#txt-search-valuei').val();
    const option = $('#search-item-by').val();

    const items = getItemByOption(option, value);

    if (Array.isArray(items) && items.length > 0) {
        $('#confirm-delete-model .modal-body').text('Are you sure you want to delete this item ?');
        $('#confirm-delete-model').modal('show');

        $('#confirm-delete-btn').one('click', function () {
            for (const item of items) {
                $.ajax({
                    url: "http://localhost:8080/Application1_Web_exploded/item?itemCode="+ item.itemCode,
                    method: 'DELETE',
                    success: function (response) {
                        showToast('success', 'Item deleted successfully !');
                        $('#confirm-delete-model').modal('hide');
                        loadItemDatabase();
                        loadItemCount();
                        initializeOrderComboBoxes();
                    },
                    error: function (error) {
                        console.log(error)
                    }
                })
            }
        });
    } else if (items && !Array.isArray(items)) {
        $('#confirm-delete-model .modal-body').text('Are you sure you want to delete this item ?');
        $('#confirm-delete-model').modal('show');

        $('#confirm-delete-btn').one('click', function () {
            $.ajax({
                url: "http://localhost:8080/Application1_Web_exploded/item?itemCode="+ items.itemCode,
                method: 'DELETE',
                success: function (response) {
                    showToast('success', 'Item deleted successfully !');
                    $('#confirm-delete-model').modal('hide');
                    loadItemDatabase();
                    loadItemCount();
                    initializeOrderComboBoxes();
                },
                error: function (error) {
                    console.log(error)
                }
            })
        });
    } else {
        showToast('error', `Item ${option} not found !`);
        loadAllItems();
    }
});

// Load All Items
$('#load-all-item-btn').on('click', function () {
    loadAllItems();
    showToast('success', 'All items loaded successfully !');
});
