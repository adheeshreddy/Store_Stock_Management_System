import React, { useEffect, useState, useMemo, useRef } from "react";
import api from "./api/axios";

function useDebounce(value, delay = 300) {
  const [debouncedValue, setDebouncedValue] = useState(value);
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);
  return debouncedValue;
}

const AddPurchase = () => {
  const [supplierQuery, setSupplierQuery] = useState("");
  const [productQuery, setProductQuery] = useState("");
  const [supplierResults, setSupplierResults] = useState([]);
  const [productResults, setProductResults] = useState([]);
  const [selectedSupplier, setSelectedSupplier] = useState(null);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [qty, setQty] = useState("");
  const [unitTax, setUnitTax] = useState("");
  const [unitGst, setUnitGst] = useState("");
  const [expiry, setExpiry] = useState("");
  const [createdBy, setCreatedBy] = useState("");
  const [createdAt, setCreatedAt] = useState(
    new Date().toISOString().slice(0, 10)
  );
  const [currEditingItemIdx,setCurrEditingItemIdx]=useState("");
  const [cart, setCart] = useState([]);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState({});
  const [dropdownOpen, setDropdownOpen] = useState({
    supplier: false,
    product: false,
  });

  const supplierRef = useRef(null);
  const productRef = useRef(null);
  const username = localStorage.getItem("username");
  const debouncedSupplier = useDebounce(supplierQuery, 300);
  const debouncedProduct = useDebounce(productQuery, 300);

  useEffect(()=>{
    if(username){
      setCreatedBy(username);
    }
  },[username]);
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (supplierRef.current && !supplierRef.current.contains(e.target)) {
        setDropdownOpen((p) => ({ ...p, supplier: false }));
      }
      if (productRef.current && !productRef.current.contains(e.target)) {
        setDropdownOpen((p) => ({ ...p, product: false }));
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // fetch suppliers
  useEffect(() => {
    const fetchSuppliers = async () => {
      if (!debouncedSupplier) return setSupplierResults([]);
      try {
        const res = await api.get("/suppliers", {
          params: { searchKey: debouncedSupplier },
        });
        setSupplierResults(res.data || []);
      } catch {
        setSupplierResults([]);
      }
    };
    fetchSuppliers();
  }, [debouncedSupplier]);

  // fetch products
  useEffect(() => {
    const fetchProducts = async () => {
      if (!debouncedProduct) return setProductResults([]);
      try {
        const res = await api.get("/product", {
          params: { product: debouncedProduct },
          headers: { "Content-Type": "text/plain" },
        });
        const active = (res.data || []).filter(
          (p) => (p.status || "").toUpperCase() === "A"
        );
        setProductResults(active);
      } catch {
        setProductResults([]);
      }
    };
    fetchProducts();
  }, [debouncedProduct]);

  const totals = useMemo(() => {
    const acc = cart.reduce(
      (sum, c) => ({
        taxable: sum.taxable + c.taxableAmount,
        gst: sum.gst + c.gstAmount,
        total: sum.total + c.totalAmount,
      }),
      { taxable: 0, gst: 0, total: 0 }
    );
    return {
      totalTaxable: acc.taxable.toFixed(2),
      totalGst: acc.gst.toFixed(2),
      totalAmount: acc.total.toFixed(2),
    };
  }, [cart]);

  const selectSupplier = (s) => {
    if (cart.length > 0) {
      if (!window.confirm("Changing supplier clears all items. Continue?"))
        return;
      setCart([]);
    }
    setSelectedSupplier(s);
    setSupplierQuery(`${s.name} (${s.id})`);
    setDropdownOpen((p) => ({ ...p, supplier: false }));
    setErrors((prev) => ({ ...prev, supplier: "" }));
  };

  const selectProduct = (p) => {
    if (cart.find((c) => c.productId === p.id)) {
      alert("Already added. Please edit from cart.");
      return;
    }
    setSelectedProduct(p);
    setProductQuery(`${p.name} (${p.id})`);
    setDropdownOpen((p) => ({ ...p, product: false }));
    setErrors((prev) => ({ ...prev, product: "" }));
  };

//   const addToCart = () => {
// console.log(currEditingItemIdx);
//     if(currEditingItemIdx !== ""){
//       console.log("entered 1");
//       setCart((prevCart) =>
//     prevCart.map((item, i) => {
//       // Check if the current item's index matches the one to be updated
//       if (i === currEditingItemIdx) {
//         // Return a new object with the updated quantity and recalculate totals
//         const updatedTaxable = item.unitTaxable * qty;
//         const updatedGst = item.unitGst * qty;
//         const updatedTotal = updatedTaxable + updatedGst;

//         return {
//           ...item, // Spread the original item properties
//           quantity: qty,
//           taxableAmount: updatedTaxable,
//           gstAmount: updatedGst,
//           totalAmount: updatedTotal,
//         };
//       }
//       // Otherwise, return the item unchanged
//       return item;
//     })
//   );
//     }
//     // removeItem(selectedProduct.id);
// else{
//     const quantity = Number(qty);
//     const tax = Number(unitTax);
//     const gst = Number(unitGst);

//     let newErrors = {};
//     if (!selectedSupplier) newErrors.supplier = "Supplier required";
//     if (!selectedProduct) newErrors.product = "Product required";
//     if (!quantity || quantity <= 0 || !Number.isInteger(quantity))
//       newErrors.qty = "Qty must be integer > 0";
//     if (!tax || tax <= 0) newErrors.unitTax = "Taxable > 0";
//     if (!gst || gst <= 0) newErrors.unitGst = "GST > 0";
//     if (!expiry || expiry < new Date().toISOString().slice(0, 10))
//       newErrors.expiry = "Expiry must be future date";

//     if (Object.keys(newErrors).length > 0) return setErrors(newErrors);

//     const taxableAmount = +(quantity * tax).toFixed(2);
//     const gstAmount = +(quantity * gst).toFixed(2);
//     const totalAmount = taxableAmount + gstAmount;

//     setCart((prev) => [
//       ...prev,
//       {
//         productId: selectedProduct.id,
//         productName: selectedProduct.name,
//         quantity,
//         unitTaxable: tax,
//         unitGst: gst,
//         taxableAmount,
//         gstAmount,
//         totalAmount,
//         expiry,
//       },
//     ]);
//     resetProductForm();
//   }
//   };

const addToCart = () => {
    if (currEditingItemIdx !== "") {
        const quantity = Number(qty);
        const tax = Number(unitTax);
        const gst = Number(unitGst);

        let newErrors = {};
        if (!quantity || quantity <= 0 || !Number.isInteger(quantity))
            newErrors.qty = "Qty must be integer > 0";
        if (!tax || tax <= 0) newErrors.unitTax = "Taxable > 0";
        if (!gst || gst <= 0) newErrors.unitGst = "GST > 0";
        if (!expiry || expiry < new Date().toISOString().slice(0, 10))
            newErrors.expiry = "Expiry must be future date";

        if (Object.keys(newErrors).length > 0) return setErrors(newErrors);

        setCart(prevCart =>
            prevCart.map((item, i) => {
                if (i === currEditingItemIdx) {
                    const taxableAmount = +(quantity * tax).toFixed(2);
                    const gstAmount = +(quantity * gst).toFixed(2);
                    const totalAmount = taxableAmount + gstAmount;

                    return {
                        ...item,
                        quantity,
                        unitTaxable: tax,
                        unitGst: gst,
                        taxableAmount,
                        gstAmount,
                        totalAmount,
                        expiry, 
                    };
                }
                return item;
            })
        );
        setCurrEditingItemIdx(""); 
    } else {
        const quantity = Number(qty);
        const tax = Number(unitTax);
        const gst = Number(unitGst);

        let newErrors = {};
        if (!selectedSupplier) newErrors.supplier = "Supplier required";
        if (!selectedProduct) newErrors.product = "Product required";
        if (!quantity || quantity <= 0 || !Number.isInteger(quantity))
            newErrors.qty = "Qty must be integer > 0";
        if (!tax || tax <= 0) newErrors.unitTax = "Taxable > 0";
        if (!gst || gst <= 0) newErrors.unitGst = "GST > 0";
        if (!expiry || expiry < new Date().toISOString().slice(0, 10))
            newErrors.expiry = "Expiry must be future date";

        if (Object.keys(newErrors).length > 0) return setErrors(newErrors);

        const taxableAmount = +(quantity * tax).toFixed(2);
        const gstAmount = +(quantity * gst).toFixed(2);
        const totalAmount = taxableAmount + gstAmount;

        setCart(prev => [
            ...prev,
            {
                productId: selectedProduct.id,
                productName: selectedProduct.name,
                quantity,
                unitTaxable: tax,
                unitGst: gst,
                taxableAmount,
                gstAmount,
                totalAmount,
                expiry,
            },
        ]);
    }
    resetProductForm();
};
  const resetProductForm = () => {
    setSelectedProduct(null);
    setProductQuery("");
    setQty("");
    setUnitTax("");
    setUnitGst("");
    setExpiry("");
  };

  const removeItem = (i) => setCart((prev) => prev.filter((_, idx) => idx !== i));

  const editItem = (item, i) => {
    setQty(item.quantity);
    setUnitTax(item.unitTaxable);
    setUnitGst(item.unitGst);
    setExpiry(item.expiry);
    setSelectedProduct({ id: item.productId, name: item.productName });
    setProductQuery(`${item.productName} (${item.productId})`);
    setCurrEditingItemIdx(i);
    // removeItem(i);
  };

  const clearAll = () => {
    setSelectedSupplier(null);
    setSupplierQuery("");
    setCart([]);
    resetProductForm();
    setErrors({});
  };
  const handleUpdate = (index, newQuantity) => {
  setCart((prevCart) =>
    prevCart.map((item, i) => {
      // Check if the current item's index matches the one to be updated
      if (i === index) {
        // Return a new object with the updated quantity and recalculate totals
        const updatedTaxable = item.unitTaxable * newQuantity;
        const updatedGst = item.unitGst * newQuantity;
        const updatedTotal = updatedTaxable + updatedGst;

        return {
          ...item, // Spread the original item properties
          quantity: newQuantity,
          taxableAmount: updatedTaxable,
          gstAmount: updatedGst,
          totalAmount: updatedTotal,
        };
      }
      // Otherwise, return the item unchanged
      return item;
    })
  );
};


  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!selectedSupplier) return setErrors({ supplier: "Supplier required" });
    if (cart.length === 0) return setErrors({ cart: "Add items first" });


    const payload = {
      supplierId: selectedSupplier.id,
      totalTaxableAmount: totals.totalTaxable,
      totalGst: totals.totalGst,
      totalAmount: totals.totalAmount,
      createdAt,
      createdBy,
      purchaseDetails: cart.map((c) => ({
        productId: c.productId,
        quantity: c.quantity,
        taxableAmount: c.unitTaxable,
        gstAmount: c.unitGst,
        totalAmount: c.unitGst+c.unitTaxable,
        expiry: c.expiry,
        reservedQuantity: 0,
        availableQuantity: 0,
      })),
    };

    console.log(payload);
    try {
      setSaving(true);
      const res = await api.post("/purchases", payload);
      alert(`Purchase created with ID: ${res.data}`);
      clearAll();
    } catch {
      alert("Failed to create purchase");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="container mt-4">
      <h3 className="text-center fw-bold mb-3">Add Purchase</h3>
      <form onSubmit={handleSubmit} className="bg-white p-4 shadow rounded">
     
        <div className="mb-3" ref={supplierRef}>
          <label className="form-label fw-semibold">Supplier</label>
          <input
            type="text"
            className="form-control"
            placeholder="Search supplier"
            value={supplierQuery}
            readOnly={!!selectedSupplier}
            onChange={(e) => setSupplierQuery(e.target.value)}
            onFocus={() =>
              !selectedSupplier && setDropdownOpen((p) => ({ ...p, supplier: true }))
            }
          />
          {errors.supplier && <div className="text-danger small">{errors.supplier}</div>}
          {dropdownOpen.supplier && supplierResults.length > 0 && !selectedSupplier && (
            <div className="list-group position-absolute w-50">
              {supplierResults.map((s) => (
                <button
                  key={s.id}
                  type="button"
                  className="list-group-item list-group-item-action"
                  onClick={() => selectSupplier(s)}
                >
                  {s.name} (ID: {s.id})
                </button>
              ))}
            </div>
          )}
          {selectedSupplier && (
            <button
              type="button"
              className="btn btn-warning btn-sm mt-2"
              onClick={() => {
                if (window.confirm("Change supplier? Clears items.")) clearAll();
              }}
            >
              Change Supplier
            </button>
          )}
        </div>

        <div className="mb-3" ref={productRef}>
          <label className="form-label fw-semibold">Product</label>
          <input
            type="text"
            className="form-control"
            placeholder="Search product"
            value={productQuery}
            onChange={(e) => {
              setProductQuery(e.target.value);
              setSelectedProduct(null);
              setErrors((prev) => ({ ...prev, product: "" }));
              setDropdownOpen((p) => ({ ...p, product: true }));
            }}
            onFocus={() => setDropdownOpen((p) => ({ ...p, product: true }))}
          />
          {errors.product && <div className="text-danger small">{errors.product}</div>}
          {dropdownOpen.product && productResults.length > 0 && (
            <div className="list-group position-absolute w-50">
              {productResults.map((p) => (
                <button
                  key={p.id}
                  type="button"
                  className="list-group-item list-group-item-action"
                  onClick={() => selectProduct(p)}
                >
                  {p.name} ({p.id}){" "}
                  {cart.find((c) => c.productId === p.id) && "⚠️ already added"}
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="row g-3 mb-3">
          <div className="col-md-2">
            <label className="form-label">Qty</label>
            <input
              type="number"
              className="form-control"
              min="1"
              value={qty}
              onChange={(e) => {setQty(e.target.value);
                setErrors((prev) => ({ ...prev, qty: "" }));
              }}
            />
            {errors.qty && <div className="text-danger small">{errors.qty}</div>}
          </div>
          <div className="col-md-2">
            <label className="form-label">Unit Tax</label>
            <input
              type="number"
              className="form-control"
              value={unitTax}
              onChange={(e) => {setUnitTax(e.target.value);
                setErrors((prev) => ({ ...prev, unitTax: "" }));
              }}
            />
            {errors.unitTax && <div className="text-danger small">{errors.unitTax}</div>}
          </div>
          <div className="col-md-2">
            <label className="form-label">Unit GST</label>
            <input
              type="number"
              className="form-control"
              value={unitGst}
              onChange={(e) => {setUnitGst(e.target.value);
                setErrors((prev) => ({ ...prev, unitGst: "" }));
              }}
            />
            {errors.unitGst && <div className="text-danger small">{errors.unitGst}</div>}
          </div>
          <div className="col-md-3">
            <label className="form-label">Expiry</label>
            <input
              type="date"
              className="form-control"
              value={expiry}
              min={new Date().toISOString().slice(0, 10)}
              onChange={(e) => {setExpiry(e.target.value);
                setErrors((prev) => ({ ...prev, expiry: "" }));
              }}
            />
            {errors.expiry && <div className="text-danger small">{errors.expiry}</div>}
          </div>
          <div className="col-md-2 d-flex align-items-end">
            <button type="button" className="btn btn-success w-100" onClick={addToCart}>
              ➕ Add
            </button>
          </div>
        </div>

        {cart.length > 0 && (
          <table className="table table-bordered mt-3">
            <thead className="table-light">
              <tr>
                <th>Product</th>
                <th>Qty</th>
                <th>Unit Tax</th>
                <th>Unit GST</th>
                <th>Expiry</th>
                <th>Taxable</th>
                <th>GST</th>
                <th>Total</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {cart.map((c, i) => (
                <tr key={i}>
                  <td>{c.productName} ({c.productId})</td>
                  <td>{c.quantity}</td>
                  <td>{c.unitTaxable.toFixed(2)}</td>
                  <td>{c.unitGst.toFixed(2)}</td>
                  <td>{c.expiry}</td>
                  <td>{c.taxableAmount.toFixed(2)}</td>
                  <td>{c.gstAmount.toFixed(2)}</td>
                  <td>{c.totalAmount.toFixed(2)}</td>
                  <td>
                    <button
                      type="button"
                      className="btn btn-sm btn-outline-primary me-2"
                      onClick={() => editItem(c, i)}
                    >
                      ✏️
                    </button>
                    <button
                      type="button"
                      className="btn btn-sm btn-outline-danger"
                      onClick={() => removeItem(i)}
                    >
                      🗑️
                    </button>
                  </td>
                </tr>
              ))}
              <tr className="fw-bold">
                <td colSpan={5}>Totals</td>
                <td>{totals.totalTaxable}</td>
                <td>{totals.totalGst}</td>
                <td>{totals.totalAmount}</td>
                <td>
                  <button className="btn btn-primary px-4" type="submit" disabled={saving}>
                    {saving ? "Saving..." : "Create"}
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        )}

        {errors.cart && <div className="text-danger small">{errors.cart}</div>}
       <div className="d-flex gap-2 mt-3 justify-content-end">
  <button type="button" className="btn btn-warning" onClick={() => setCart([])}>
    Clear Lines
  </button>
  <button type="button" className="btn btn-secondary" onClick={clearAll}>
    Reset Form
  </button>
</div>
      </form>
    </div>
  );
};

export default AddPurchase;