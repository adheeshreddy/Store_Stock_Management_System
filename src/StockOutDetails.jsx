import React, { useEffect, useState } from "react";
import { useParams, useNavigate,useLocation } from "react-router-dom";
import api from "./api/axios";

export default function StockOutDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [details, setDetails] = useState([]);
  const [productMap, setProductMap] = useState({});
  const [loading, setLoading] = useState(false);
  const location = useLocation();
  
  const { supplierId } = location.state || {};


  const loadProducts = async () => {
    try {
      const res = await api.get("/product");
      const map = {};
      (res.data || []).forEach((p) => {
        map[p.id] = p.name;
      });
      setProductMap(map);
    } catch (err) {
      console.error("Failed to fetch products", err);
    }
  };

  const loadDetails = async () => {
    try {
      setLoading(true);
      const res = await api.get(`/stockout/${id}/details`);
      setDetails(res.data || []);
    } catch (err) {
      console.error("Failed to fetch details", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
    loadDetails();
  }, [id]);

  return (
    <div className="container my-4">
      <h3 className="fw-bold mb-3">Details for StockOut #{id}</h3>
      <button className="btn btn-secondary mb-3" onClick={() => navigate(-1, { state: { supplierId } })}>
        ← Back
      </button>
      {loading ? (
        <div>Loading…</div>
      ) : (
        <div className="table-responsive">
          <table className="table table-bordered table-hover text-center">
            <thead className="table-light">
              <tr>
                <th>Batch</th>
                <th>Product</th>
                <th>Qty</th>
                <th>Taxable</th>
                <th>GST</th>
                <th>Total</th>
                <th>Expiry</th>
              </tr>
            </thead>
            <tbody>
              {details.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center text-muted">No details</td>
                </tr>
              ) : (
                details.map((d, i) => (
                  <tr key={i}>
                    <td>{d.batchId}</td>
                    <td>{productMap[d.productId] || "Unknown"} (ID:{d.productId})</td>
                    <td>{d.quantity}</td>
                    <td>{d.taxableAmount?.toFixed(2)}</td>
                    <td>{d.gstAmount?.toFixed(2)}</td>
                    <td>{d.totalAmount?.toFixed(2)}</td>
                    <td>{d.expiry}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}