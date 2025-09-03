import React, { useState } from "react";
import api from "./api/axios";

const AddProduct = () => {
  const [product, setProduct] = useState({ id: "", name: "", status: "A" });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const validate = () => {
    const newErrors = {};
    const alphanumericRegex = /^[A-Za-z0-9]+$/;

    if (!product.id.trim()) {
      newErrors.id = "Id is required";
    } else if (!alphanumericRegex.test(product.id.trim())) {
      newErrors.id = "Id must contain only alphanumeric characters";
    }

    if (!product.name.trim()) {
      newErrors.name = "Name is required";
    } else if (!alphanumericRegex.test(product.name.trim())) {
      newErrors.name = "Name must contain only alphanumeric characters";
    }

    if (!product.status.trim()) {
      newErrors.status = "Status is required";
    }

    return newErrors;
  };

  const handleChange = (e) => {
    setProduct({ ...product, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: "" });
  };

  const handleClear = () => {

     const confirmClear = window.confirm("Are you sure you want to clear the form?");
    if (confirmClear) {
      setProduct({ id: "", name: "", status: "A" });
      setErrors({});
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }
    setLoading(true);
    try {
      const res = await api.post("/product", product);
      alert(`Product added Successfully\nProduct id: ${res.data}`);
      setProduct({ id: "", name: "", status: "A" });
    } catch (err) {
      console.error(err.response.data);
      alert(err.response.data.details );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mt-5">
      <h3 className="text-center fw-bold mb-4">Add a New Product</h3>
      <form onSubmit={handleSubmit} className="bg-white p-4 rounded shadow">
        <div className="row g-3">
          <div className="col-md-6">
            <label htmlFor="id" className="form-label fw-semibold">Product Id</label>
            <input
              type="text"
              className={`form-control ${errors.id ? 'is-invalid' : ''}`}
              id="id"
              name="id"
              placeholder="Enter Product id"
              value={product.id}
              onChange={handleChange}
            />
            {errors.id && <div className="invalid-feedback">{errors.id}</div>}
          </div>

          <div className="col-md-6">
            <label htmlFor="name" className="form-label fw-semibold">Product Name</label>
            <input
              type="text"
              className={`form-control ${errors.name ? 'is-invalid' : ''}`}
              id="name"
              name="name"
              placeholder="Enter Product name"
              value={product.name}
              onChange={handleChange}
            />
            {errors.name && <div className="invalid-feedback">{errors.name}</div>}
          </div>

          <div className="col-md-6">
            <label htmlFor="status" className="form-label fw-semibold">Status</label>
            <select
              className={`form-select ${errors.status ? 'is-invalid' : ''}`}
              id="status"
              name="status"
              value={product.status}
              onChange={handleChange}
            >
              <option value="A">Active</option>
              <option value="I">Inactive</option>
            </select>
            {errors.status && <div className="invalid-feedback">{errors.status}</div>}
          </div>
        </div>

        <div className="d-flex justify-content-center gap-3 mt-4">
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? "Adding Product..." : "Add Product"}
          </button>
          <button className="btn btn-danger" type="button" onClick={handleClear}>
            Clear
          </button>
        </div>
      </form>
    </div>
  );
};

export default AddProduct;