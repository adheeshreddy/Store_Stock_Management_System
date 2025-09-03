import React, { useEffect, useState } from "react";
import AsyncSelect from "react-select/async";
import "bootstrap/dist/css/bootstrap.min.css";
import api from "./api/axios";

export default function CreateProforma() {
    const [products, setProducts] = useState([]);
    const [batches, setBatches] = useState([]);
    const [selectedSupplier, setSelectedSupplier] = useState(null);
    const [productId, setProductId] = useState("");
    const [batchId, setBatchId] = useState("");
    const [quantity, setQuantity] = useState("");
    const [items, setItems] = useState([]);
    const [selectedBatch, setSelectedBatch] = useState(null);
    const [availableQuantities, setAvailableQuantities] = useState({});
    const [validationError, setValidationError] = useState("");
    const [type, setType] = useState("creation");

    const [editingIndex, setEditingIndex] = useState(null);
    useEffect(() => {
        loadSuppliers("");
        loadProducts("");
    }, []);

    const loadSuppliers = async (inputValue) => {
        try {
            const res = await api.get("/suppliers", {
                params: { searchKey: inputValue || "" },
            });
            return res.data.map((s) => ({
                value: s.id,
                label: `${s.id} - ${s.name}`,
            }));
        } catch {
            return [];
        }
    };

    const loadProducts = async (inputValue) => {
        try {
            const res = await api.get("/product", {
                params: { product: inputValue },
                headers: { "Content-Type": "text/plain" },
            });
            const options = res.data.map((p) => ({
                value: p.id,
                label: `${p.id} - ${p.name}`,
            }));
            setProducts(options);
            return options;
        } catch {
            return [];
        }
    };

    const fetchBatches = async (productId) => {
        try {
            if (!productId) {
                setBatches([]);
                return;
            }
            const res = await api.get("/purchases/batches", {
                params: { productId },
            });
            setBatches(res.data);
            setAvailableQuantities((prev) => {
                const updated = { ...prev };
                res.data.forEach((b) => {
                    if (!(b.batchId in updated)) {
                        updated[b.batchId] = b.availableQuantity;
                    }
                });
                return updated;
            });
        } catch {
            setBatches([]);
        }
    };

    const handleProductChange = (selectedOption) => {
        setProductId(selectedOption ? selectedOption.value : "");
        fetchBatches(selectedOption ? selectedOption.value : "");
        setBatchId("");
        setSelectedBatch(null);
        setQuantity("");
        setValidationError("");
    };

    const handleBatchChange = (e) => {
        const selected = batches.find((b) => String(b.batchId) === String(e.target.value));
        setBatchId(e.target.value);
        setSelectedBatch(selected || null);
        setQuantity("");
        setValidationError("");
    };

    const handleQuantityChange = (e) => {
        const value = e.target.value;
        setQuantity(value);
        if (selectedBatch && Number(value) > availableQuantities[selectedBatch.batchId]) {
            setValidationError(`Max available: ${availableQuantities[selectedBatch.batchId]}`);
        }
        else if (Number(value) <= 0) {
            setValidationError(`Invalid Quantity: ${Number(value)}`);
        }
        else {
            setValidationError("");
        }
    };


    const handleAddItem = () => {
        if (!productId || !batchId || !quantity || validationError) return;
        const selectedProduct = products.find((p) => String(p.value) === String(productId));
        const existingIndex = items.findIndex(
            (item) => item.productId === productId && item.batchId === batchId
        );

        const unitTaxable = selectedBatch.taxableAmount;
        const unitGst = selectedBatch.gstAmount;
        const unitTotal = unitTaxable + unitGst;

        if (existingIndex !== -1) {
            const updatedItems = [...items];
            if (type === 'created')
                updatedItems[existingIndex].quantity += Number(quantity);
            if (type === 'edit')
                updatedItems[existingIndex].quantity = Number(quantity);
                setType("created");
                setItems(updatedItems);
        } else {
            const newItem = {
                productId,
                productName: selectedProduct ? selectedProduct.label : "",
                batchId,
                quantity: Number(quantity),
                taxableAmount: unitTaxable,
                gstAmount: unitGst,
                totalAmount: unitTotal,
                expiry: selectedBatch.expiry,
            };
            setItems([...items, newItem]);
        }

        setAvailableQuantities((prev) => ({
            ...prev,
            [batchId]: prev[batchId] - Number(quantity),
        }));

        setProductId("");
        setBatchId("");
        setQuantity("");
        setBatches([]);
        setSelectedBatch(null);
        setValidationError("");
    };

    const handleRemoveItem = (index) => {
        const removedItem = items[index];
        setItems(items.filter((_, i) => i !== index));
        setAvailableQuantities((prev) => ({
            ...prev,
            [removedItem.batchId]: (prev[removedItem.batchId] || 0) + removedItem.quantity,
        }));
    };

    const handleEditItem = (index) => {
        const item = items[index];
        const pid = item.productId;
        setProductId(item.productId);
        setBatchId(item.batchId);
        setQuantity(item.quantity);
        setSelectedBatch({
            batchId: item.batchId,
            expiry: item.expiry,
            taxableAmount: item.taxableAmount,
            gstAmount: item.gstAmount,
        });
        fetchBatches(item.productId);
        setEditingIndex(index);
        setType("edit");
        setAvailableQuantities((prev) => ({
            ...prev,
            [item.batchId]: prev[item.batchId] + item.quantity,
        }));
        // setItems((prev) => prev.filter((i) => !(String(i.productId) === pid && String(i.batchId) === String(item.batchId))));
    };

    const handleClearSupplier = () => {
        if (!selectedSupplier) return;
        if (window.confirm("Do you really want to change the Supplier ?")) {
            setSelectedSupplier(null);
        }
    };

    const handleClearForm = () => {
        if (window.confirm("Clear the entire form?")) {
            setSelectedSupplier(null);
            setProductId("");
            setBatchId("");
            setQuantity("");
            setItems([]);
            setBatches([]);
            setSelectedBatch(null);
            setAvailableQuantities({});
            setValidationError("");
        }
    };

    const username = localStorage.getItem("username");

    const handleSubmitProforma = async () => {
        if (!selectedSupplier || items.length === 0) return;
        const supplierId = selectedSupplier.value;
        const totalTaxable = items.reduce((sum, item) => sum + item.taxableAmount * item.quantity, 0);
        const totalGst = items.reduce((sum, item) => sum + item.gstAmount * item.quantity, 0);
        const totalAmount = totalTaxable + totalGst;

        const confirmSubmit = window.confirm(
            `Submit proforma?\n\nTaxable: ${totalTaxable.toFixed(2)}\nGST: ${totalGst.toFixed(2)}\nTotal: ${totalAmount.toFixed(2)}`
        );
        if (!confirmSubmit) return;

        try {
            const payload = {
                supplierId,
                totalTaxableAmount: totalTaxable,
                totalGst: totalGst,
                totalAmount: totalAmount,
                createdBy: username,
                proformaDetails: items.map((item) => ({
                    headerId: null,
                    batchId: item.batchId,
                    productId: item.productId,
                    quantity: item.quantity,
                    taxableAmount: item.taxableAmount,
                    gstAmount: item.gstAmount,
                    totalAmount: item.totalAmount,
                    expiry: item.expiry,
                    status: "C",
                })),
            };
            await api.post("/proformas", payload);
            alert("Proforma created successfully!");
            setItems([]);
            setSelectedSupplier(null);
        } catch (err) {
            alert('Error : ' + err.response.data.error + '\n' + 'Message : ' + err.response.data.details);
        }
    };

    const showClearForm = selectedSupplier || productId || batchId || quantity || items.length > 0;
    const totalTaxable = items.reduce((sum, item) => sum + item.taxableAmount * item.quantity, 0);
    const totalGst = items.reduce((sum, item) => sum + item.gstAmount * item.quantity, 0);
    const totalAmount = totalTaxable + totalGst;

    return (
        <div className="container mt-4 p-4 border rounded shadow-sm bg-light">
            <h3 className="mb-4 text-center ">Create Proforma</h3>
            <div className="row g-3 align-items-center">
                <div className="col-md-3 d-flex align-items-center">
                    <div className="flex-grow-1">
                        <label className="form-label">Supplier</label>
                        <AsyncSelect
                            key={selectedSupplier ? selectedSupplier.value : "supplier-select"}
                            defaultOptions
                            loadOptions={loadSuppliers}
                            onChange={(option) => setSelectedSupplier(option)}
                            value={selectedSupplier}
                            placeholder="Select Supplier"
                            isClearable={false}
                            isDisabled={!!selectedSupplier}
                        />
                    </div>
                    {selectedSupplier && (
                        <button
                            className="btn btn-sm btn-outline-danger ms-2 mt-4"
                            type="button"
                            onClick={handleClearSupplier}
                        >
                            ✕
                        </button>
                    )}
                </div>

                <div className="col-md-3">
                    <label className="form-label">Product</label>
                    <AsyncSelect
                        cacheOptions
                        defaultOptions
                        loadOptions={loadProducts}
                        onChange={handleProductChange}
                        value={products.find((p) => p.value === productId) || null}
                        placeholder="Select Product"
                        isClearable
                    />
                </div>

                <div className="col-md-3">
                    <label className="form-label">Batch</label>
                    <select
                        className="form-select"
                        value={batchId}
                        onChange={handleBatchChange}
                        disabled={!productId}
                    >
                        <option value="">Select Batch</option>
                        {batches.length > 0 ? (
                            batches.map((b) => (
                                <option key={b.batchId} value={b.batchId}>
                                    {b.batchId} - Avl: {availableQuantities[b.batchId] ?? b.availableQuantity}
                                </option>
                            ))
                        ) : (
                            <option disabled>No batches</option>
                        )}
                    </select>
                </div>

                <div className="col-md-3">
                    <label className="form-label">Quantity</label>
                    <input
                        type="number"
                        className="form-control"
                        placeholder="Enter quantity"
                        value={quantity}
                        onChange={handleQuantityChange}
                        disabled={!batchId}
                        min="1"
                        step="1"
                        onKeyDown={(e) => {
                            if (["e", "E", ".", "+", "-"].includes(e.key)) e.preventDefault();
                        }}
                    />
                </div>
            </div>

            <div className="row g-1 mt-0">
                <div className="col-md-3"></div>
                <div className="col-md-3"></div>
                <div className="col-md-3">
                    {selectedBatch && <div className="form-text">Expiry: {selectedBatch.expiry}</div>}
                </div>
                <div className="col-md-3">
                    {validationError && <div className="text-danger small">{validationError}</div>}
                    {selectedBatch && quantity > 0 && !validationError && (
                        <div className="form-text">
                            Taxable: {(selectedBatch.taxableAmount * quantity).toFixed(2)} | GST:{" "}
                            {(selectedBatch.gstAmount * quantity).toFixed(2)} | Total:{" "}
                            {((selectedBatch.taxableAmount + selectedBatch.gstAmount) * quantity).toFixed(2)}
                        </div>
                    )}
                </div>
            </div>

            <div className="row g-3 mt-3">
                <div className="col-md-3 ms-auto">
                    <button
                        className="btn btn-success w-100"
                        onClick={handleAddItem}
                        disabled={!productId || !batchId || !quantity || validationError}
                    >
                        Add Item
                    </button>
                </div>
            </div>

            {items.length > 0 && (
                <div className="mt-4">
                    <h5>Added Products</h5>
                    <table className="table table-bordered mt-2 text-center align-middle">
                        <thead className="table-primary">
                            <tr>
                                <th>Product</th>
                                <th>Batch</th>
                                <th>Expiry</th>
                                <th>Quantity</th>
                                <th>Total Taxable</th>
                                <th>Total GST</th>
                                <th>Total Amount</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {items.map((item, index) => (
                                <tr key={index}>
                                    <td>{item.productName}</td>
                                    <td>{item.batchId}</td>
                                    <td>{item.expiry}</td>
                                    <td>{item.quantity}</td>
                                    <td>{(item.taxableAmount * item.quantity).toFixed(2)}</td>
                                    <td>{(item.gstAmount * item.quantity).toFixed(2)}</td>
                                    <td>
                                        {((item.taxableAmount + item.gstAmount) * item.quantity).toFixed(2)}
                                    </td>
                                    <td>
                                        <button
                                            className="btn btn-sm btn-outline-primary me-2"
                                            onClick={() => handleEditItem(index)}
                                        >
                                            ✏
                                        </button>
                                        <button
                                            className="btn btn-sm btn-outline-danger"
                                            onClick={() => handleRemoveItem(index)}
                                        >
                                            ✕
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            <tr className="fw-bold table-light">
                                <td colSpan="4" className="text-end">
                                    Totals:
                                </td>
                                <td>{totalTaxable.toFixed(2)}</td>
                                <td>{totalGst.toFixed(2)}</td>
                                <td>{totalAmount.toFixed(2)}</td>
                                <td></td>
                            </tr>
                        </tbody>
                    </table>

                    <div className="mt-3 d-flex justify-content-end gap-2">
                        {showClearForm && (
                            <button className="btn btn-danger" type="button" onClick={handleClearForm}>
                                Clear Form
                            </button>
                        )}
                        <button className="btn btn-primary" onClick={handleSubmitProforma}>
                            Submit Proforma
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}