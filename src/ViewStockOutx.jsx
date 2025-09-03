import React, { useEffect, useMemo, useState } from "react";

import { useParams, useNavigate } from "react-router-dom";
import api from "./api/axios";

const ViewStockOutx = () => {

  const { id } = useParams();
  const navigate = useNavigate();
  const [headers, setHeaders] = useState([]);
  const [supplierMap, setSupplierMap] = useState({});

  const loadAllSuppliers = async () => {
    try {
      const res = await api.get("/suppliers");
      const map = {};
      (res.data || []).forEach((s) => {
        map[s.id] = s.name;
      });
      setSupplierMap(map);
    } catch (e) {
      console.error("Failed to fetch all suppliers", e);
    }
  };


  //   useEffect(() => {
  //     const onDocClick = (e) => {
  //       if (!supplierBoxRef.current) return;
  //       if (!supplierBoxRef.current.contains(e.target)) setSupplierOpen(false);
  //     };
  //     document.addEventListener("mousedown", onDocClick);
  //     return () => document.removeEventListener("mousedown", onDocClick);
  //   }, []);

  useEffect(() => { handlePickSupplier(id); loadAllSuppliers(); }, []);

  const handlePickSupplier = async (id) => {
    // setSelectedSupplier(s);
    // setSupplierQuery(`${s.name} (${s.id})`);
    // setSupplierOpen(false);
    try {
      //   setLoading(true);
      const res = await api.get(`/stockout/suppliers/${id}`);
      setHeaders(res.data || []);
    } catch (err) {
      console.error("Failed to load stock-out headers", err);
      setHeaders([]);
      alert("Failed to load stock-outs for this supplier.");
    } finally {
      //   setLoading(false);
    }
  };

  const { invoiceHeaders, returnHeaders } = useMemo(() => {
    const inv = [];
    const ret = [];
    (headers || []).forEach((h) => {
      if ((h.status || "").toUpperCase() === "I") inv.push(h);
      else if ((h.status || "").toUpperCase() === "R") ret.push(h);
    });
    return { invoiceHeaders: inv, returnHeaders: ret };
  }, [headers]);

  return (
    <div className="container my-4">
      <h2 className="text-center fw-bold mb-4">Stock-Outs</h2>

      {/*}  <div className="mb-3" ref={supplierBoxRef}>
        <label className="form-label fw-bold">Supplier</label>
        <div className="position-relative">
          <input
            className="form-control"
            placeholder="Search supplier by id or name…"
            value={supplierQuery}
            onChange={(e) => {
              setSupplierQuery(e.target.value);
              setSelectedSupplier(null);
              setSupplierOpen(true);
            }}
            onFocus={() => setSupplierOpen(true)}
            autoComplete="off"
          />
          {/* {supplierOpen && supplierResults.length > 0 && (
            <ul className="list-group position-absolute w-100 mt-1" style={{ zIndex: 1000 }}>
              {supplierResults.map((s) => (
                <li
                  key={s.id}
                  className="list-group-item list-group-item-action"
                  onClick={() => handlePickSupplier(s)}
                >
                  {s.name} (ID: {s.id})
                </li>
              ))}
            </ul>
          )} }
        </div>
      </div>*/}

      {/* //   {loading && <div className="text-muted my-2">Loading…</div>} */}

      <section className="mt-4">
        <button className="btn btn-secondary mb-3" onClick={() => navigate(-1)}>
          ← Back
        </button>
        <h4 className="fw-bold">Invoice Headers</h4>
        <div className="table-responsive">
          <table className="table table-bordered table-hover text-center">
            <thead className="table-light">
              <tr>
                <th>StockOut Id</th>
                <th>Proforma Id</th>
                <th>Supplier</th>
                <th>Taxable</th>
                <th>GST</th>
                <th>Total</th>
                <th>Approved At</th>
                <th>Approved By</th>
                <th>Open</th>
              </tr>
            </thead>
            <tbody>
              {invoiceHeaders.length === 0 ? (
                <tr>
                  <td colSpan={9} className="text-center text-muted">
                    No invoice headers
                  </td>
                </tr>
              ) : (
                invoiceHeaders.map((h) => (
                  <tr key={h.id}>
                    <td>{h.id}</td>
                    <td>{h.headerId}</td>
                    <td>{supplierMap[h.supplierId] || "Unknown"} (ID:{h.supplierId})</td>
                    <td>{h.totalTaxableAmount?.toFixed(2)}</td>
                    <td>{h.totalGst?.toFixed(2)}</td>
                    <td>{h.totalAmount?.toFixed(2)}</td>
                    <td>{h.approvedAt}</td>
                    <td>{h.approvedBy}</td>
                    <td>
                      <button
                        className="btn btn-sm btn-primary"
                        onClick={() => navigate(`/stockout/${h.id}/details`)}
                      >
                        Details
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section className="mt-5">
        <h4 className="fw-bold">Return Headers</h4>
        <div className="table-responsive">
          <table className="table table-bordered table-hover text-center">
            <thead className="table-light">
              <tr>
                <th>StockOut Id</th>
                <th>Proforma Id</th>
                <th>Supplier</th>
                <th>Taxable</th>
                <th>GST</th>
                <th>Total</th>
                <th>Approved At</th>
                <th>Approved By</th>
                <th>Open</th>
              </tr>
            </thead>
            <tbody>
              {returnHeaders.length === 0 ? (
                <tr>
                  <td colSpan={9} className="text-center text-muted">
                    No return headers
                  </td>
                </tr>
              ) : (
                returnHeaders.map((h) => (
                  <tr key={h.id}>
                    <td>{h.id}</td>
                    <td>{h.headerId}</td>
                    <td>{supplierMap[h.supplierId] || "Unknown"} (ID:{h.supplierId})</td>
                    <td>{h.totalTaxableAmount?.toFixed(2)}</td>
                    <td>{h.totalGst?.toFixed(2)}</td>
                    <td>{h.totalAmount?.toFixed(2)}</td>
                    <td>{h.approvedAt}</td>
                    <td>{h.approvedBy}</td>
                    <td>
                      <button
                        className="btn btn-sm btn-primary"
                        onClick={() => navigate(`/stockout/${h.id}/details`)}
                      >
                        Details
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
};

export default ViewStockOutx;