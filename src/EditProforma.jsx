import React, { useState, useEffect } from "react";
import AsyncSelect from "react-select/async";
import { useParams, useNavigate } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
import api from "./api/axios";

export default function EditProforma() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [products, setProducts] = useState([]);
    const [batches, setBatches] = useState([]);
    const [supplierId, setSupplierId] = useState("");
    const [productId, setProductId] = useState("");
    const [selectedProductOption, setSelectedProductOption] = useState(null);
    const [batchId, setBatchId] = useState("");
    const [quantity, setQuantity] = useState("");
    const [items, setItems] = useState([]);
    const [selectedBatch, setSelectedBatch] = useState(null);
    const [loading, setLoading] = useState(true);
    const [editingProductId, setEditingProductId] = useState(null);
    const [supplierName, setSupplierName] = useState("");
    const [quantityError, setQuantityError] = useState("");
    const [productsX, setProductsX] = useState([]);
    const [type,setType]=useState("created");

    useEffect(() => {
        fetchProforma();
        loadProductsX();
    }, []);

    const loadProducts = async (inputValue) => {
        try {
            const res = await api.get("/product", {
                params: { product: inputValue || "" },
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

    const loadProductsX = async () => {
        try {
            const res = await api.get("/product", {
                headers: { "Content-Type": "text/plain" },
            });
            setProductsX(res.data || []);
        } catch (err) {
            alert('Error : ' + err.response.data.error + '\n' + 'Message : ' + err.response.data.details);
        }
    };
    const productMap = productsX.reduce((map, p) => {
        map[p.id] = p.name;
        return map;
    }, {});

    const fetchProforma = async () => {
        try {
            const res = await api.get(`/proformas/${id}`);
            const data = res.data;
            setSupplierId(String(data.supplierId || ""));
            if (data.supplierId) {
                try {
                    const supRes = await api.get(`/suppliers/${data.supplierId}`);
                    setSupplierName(supRes?.data || "");
                } catch {
                    setSupplierName("");
                }
            }

            const enrichedItems = (data.proformaDetails || []).map((item) => ({
                productId: String(item.productId),
                productName: item.productName || "",
                batchId: String(item.batchId),
                quantity: Number(item.quantity),
                taxableAmount: Number(item.taxableAmount),
                gstAmount: Number(item.gstAmount),
                totalAmount: Number(item.totalAmount),
                expiry: item.expiry,
            }));
            setItems(enrichedItems);
        } catch (err) {
            alert('Error : ' + err.response.data.error + '\n' + 'Message : ' + err.response.data.details);
            navigate("/view/proforma");
        } finally {
            setLoading(false);
        }
    };

    const fetchBatches = async (pid) => {
        try {
            if (!pid) {
                setBatches([]);
                return [];
            }
            const res = await api.get("/purchases/batches", {
                params: { productId: pid },
            });
            setBatches(res.data || []);
            return res.data || [];
        } catch {
            setBatches([]);
            return [];
        }
    };

    const editItem = async (item) => {
        const pid = String(item.productId);
        console.log(item.productName);
        const option = { value: pid, label: `${pid} - ${productMap[pid]}` };
        setEditingProductId(pid);
        setSelectedProductOption(option);
        setProductId(pid);
        setQuantity(String(item.quantity));
        setProducts((prev) => {
            const exists = prev.find((p) => String(p.value) === pid);
            return exists ? prev : [...prev, option];
        });
        const list = await fetchBatches(pid);
        const selected = (list || []).find((b) => String(b.batchId) === String(item.batchId));
        setBatchId(String(item.batchId));
        setSelectedBatch(selected || null);
        setType('edit');
        // setItems((prev) =>
        //     prev.filter(
        //         (i) => !(String(i.productId) === pid && String(i.batchId) === String(item.batchId))
        //     )
        // );
    };

    const removeItem = (itemToRemove) => {
        setItems((prevItems) =>
            prevItems.filter(
                (item) =>
                    !(item.productId === itemToRemove.productId && item.batchId === itemToRemove.batchId)
            )
        );
    };

    const handleProductChange = (opt) => {
        setSelectedProductOption(opt);
        const pid = opt ? String(opt.value) : "";
        setProductId(pid);
        fetchBatches(pid);
        setBatchId("");
        setSelectedBatch(null);
        setQuantity("");
        setQuantityError("");
    };

    const handleBatchChange = (e) => {
        const val = String(e.target.value);
        const selected = batches.find((b) => String(b.batchId) === val);
        setBatchId(val);
        setSelectedBatch(selected || null);
        setQuantity("");
        setQuantityError("");
    };

    const handleQuantityChange = (val) => {
        setQuantity(val);
        if (!val) {
            setQuantityError("");
            return;
        }
        const qtyNum = Number(val);
        if (Number.isNaN(qtyNum) || qtyNum <= 0) {
            setQuantityError("Quantity must be a positive number");
        } else if (selectedBatch && qtyNum > Number(selectedBatch.availableQuantity)) {
            setQuantityError(`Quantity cannot exceed available quantity: ${selectedBatch.availableQuantity}`);
        } else {
            setQuantityError("");
        }
    };

    const handleAddItem = () => {
        if (!productId || !batchId || !quantity) {
            alert("Please fill all fields");
            return;
        }
        if (quantityError) return;

        let qtyNum = Number(quantity);
        const selectedProduct = selectedProductOption || products.find((p) => String(p.value) === String(productId));
        console.log(qtyNum);

        const existingIndex = items.findIndex(
            (item) => String(item.productId) === String(productId) && String(item.batchId) === String(batchId)
        );
        
        const prev=(items[existingIndex].quantity)+qtyNum;
        console.log(prev);
        if(type==='created'){
            qtyNum=prev;
        }
        if(type==='edit'){
            qtyNum=qtyNum;
            setType('created')
        }
        const unitTaxable = Number(selectedBatch?.taxableAmount || 0);
        const unitGst = Number(selectedBatch?.gstAmount || 0);
        const unitTotal = unitTaxable + unitGst;
        const newRow = {
            productId: String(productId),
            productName: selectedProduct ? selectedProduct.label.split(" - ").slice(1).join(" - ") : "",
            batchId: String(batchId),
            quantity: qtyNum,
            taxableAmount: unitTaxable,
            gstAmount: unitGst,
            totalAmount: unitTotal,
            expiry: selectedBatch?.expiry,
        };
        if (existingIndex !== -1) {
            const updated = [...items];
            updated[existingIndex] = newRow;
            setItems(updated);
        } else {
            setItems((prev) => [...prev, newRow]);
        }
        setProductId("");
        setSelectedProductOption(null);
        setBatchId("");
        setQuantity("");
        setBatches([]);
        setSelectedBatch(null);
        setEditingProductId(null);
        setQuantityError("");
    };

    const totalTaxable = items.reduce((sum, item) => sum + item.taxableAmount * item.quantity, 0);
    const totalGst = items.reduce((sum, item) => sum + item.gstAmount * item.quantity, 0);
    const totalAmount = totalTaxable + totalGst;
    const username = localStorage.getItem("username");

    const handleSubmitProforma = async () => {
        if (!supplierId || items.length === 0) {
            alert("Please select supplier and add items");
            return;
        }
        const totalTaxable = items.reduce((sum, item) => sum + Number(item.taxableAmount) * Number(item.quantity), 0);
        const totalGst = items.reduce((sum, item) => sum + Number(item.gstAmount) * Number(item.quantity), 0);
        const totalAmount = totalTaxable + totalGst;
        const confirmSubmit = window.confirm(
            `Do you want to submit this proforma?\n\nTotal Taxable: ${totalTaxable.toFixed(2)}\nTotal GST: ${totalGst.toFixed(
                2
            )}\nGrand Total: ${totalAmount.toFixed(2)}`
        );
        if (!confirmSubmit) return;
        try {
            const payload = {
                id: Number(id),
                supplierId: Number(supplierId),
                totalTaxableAmount: totalTaxable,
                totalGst: totalGst,
                totalAmount: totalAmount,
                createdBy: username,
                proformaDetails: items.map((item) => ({
                    headerId: Number(id),
                    batchId: Number(item.batchId),
                    productId: String(item.productId),
                    quantity: Number(item.quantity),
                    taxableAmount: Number(item.taxableAmount),
                    gstAmount: Number(item.gstAmount),
                    totalAmount: Number(item.totalAmount),
                    expiry: item.expiry,
                    status: "E",
                })),
            };
            await api.put("/proformas", payload);
            alert("Proforma updated successfully!");
            navigate("/view/proforma");
        } catch (err) {
            alert('Error : ' + err.response.data.error + '\n' + 'Message : ' + err.response.data.details);
        }
    };

    if (loading) return <div className="p-4">Loading...</div>;

    return (
        <div className="container mt-4 p-4 border rounded shadow-sm bg-light">
            <button className="btn btn-secondary mb-3" onClick={() => navigate(-1)}>
                ⬅ Back
            </button>
            <h3 className="mb-4 text-center">Edit Proforma</h3>
            <div className="row g-3 align-items-center">
                <div className="col-md-3 d-flex align-items-center">
                    <div className="flex-grow-1">
                        <label className="form-label">Supplier :</label>
                        <input
                            type="text"
                            className="form-control"
                            value={supplierName ? `${supplierName} (ID: ${supplierId})` : supplierId}
                            disabled
                        />
                    </div>
                </div>
                <div className="col-md-3">
                    <label className="form-label">Product</label>
                    <AsyncSelect
                        cacheOptions
                        defaultOptions
                        loadOptions={loadProducts}
                        onChange={handleProductChange}
                        value={selectedProductOption}
                        placeholder="Search & Select Product"
                        isClearable
                    />
                </div>
                <div className="col-md-3">
                    <label className="form-label">Batch</label>
                    <select className="form-select" value={batchId} onChange={handleBatchChange} disabled={!productId}>
                        <option value="">Select Batch</option>
                        {batches.length > 0 ? (
                            batches.map((b) => (
                                <option key={b.batchId} value={b.batchId}>
                                    {b.batchId} - Available: {b.availableQuantity} - Exp: {b.expiry}
                                </option>
                            ))
                        ) : (
                            <option disabled>No batches available</option>
                        )}
                    </select>
                </div>
                <div className="col-md-3">
                    <label className="form-label">Quantity</label>
                    <input
                        type="number"
                        className={`form-control ${quantityError ? "is-invalid": ""}`}
                        placeholder="Enter quantity"
                        value={quantity}
                        onChange={(e) => handleQuantityChange(e.target.value)}
                        disabled={!batchId}
                        min="1"
                        step="1"
                        onKeyDown={(e) => {
                            if (["e", "E", ".", "+", "-"].includes(e.key)) e.preventDefault();
                        }}
                    />
                    {quantityError && <div className="invalid-feedback">{quantityError}</div>}
                </div>
            </div>
            <div className="row g-1 mt-0">
                <div className="col-md-3"></div>
                <div className="col-md-3"></div>
                <div className="col-md-3">
                    {selectedBatch && <div className="form-text">Expiry: {selectedBatch.expiry}</div>}
                </div>
                <div className="col-md-3">
                    {selectedBatch && quantity > 0 && !quantityError && (
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
                    <button className="btn btn-success w-100" onClick={handleAddItem}>
                        Add Item
                    </button>
                </div>
            </div>
            {items.length > 0 && (
                <div className="mt-4">
                    <h5>Added Products</h5>
                    <table className="table table-bordered mt-2">
                        <thead>
                            <tr>
                                <th>Product</th>
                                <th>Batch ID</th>
                                <th>Expiry</th>
                                <th>Quantity</th>
                                <th>Total Taxable</th>
                                <th>Total GST</th>
                                <th>Total Amount</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {items.map((item, index) => (
                                <tr key={index}>
                                    <td>{item.productId}-{productMap[item.productId]}</td>
                                    <td>{item.batchId}</td>
                                    <td>{item.expiry}</td>
                                    <td>{item.quantity}</td>
                                    <td>{(item.taxableAmount * item.quantity).toFixed(2)}</td>
                                    <td>{(item.gstAmount * item.quantity).toFixed(2)}</td>
                                    <td>{((item.taxableAmount + item.gstAmount) * item.quantity).toFixed(2)}</td>
                                    <td>
                                        <button className="btn btn-primary btn-sm me-2" onClick={() => editItem(item)}>
                                            ✏
                                        </button>
                                        <button className="btn btn-danger btn-sm" onClick={() => removeItem(item)}>
                                            <i className="bi bi-trash"></i>
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
                </div>
            )}
            <div className="mt-3 text-end">
                <button
                    className="btn btn-primary px-4"
                    onClick={handleSubmitProforma}
                    disabled={items.length === 0 || !supplierId}
                >
                    Submit Proforma
                </button>
            </div>
        </div>
    );
}