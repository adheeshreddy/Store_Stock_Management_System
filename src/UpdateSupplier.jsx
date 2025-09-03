import React, { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import api from "./api/axios";

export default function UpdateSupplier() {
  const location = useLocation();
  const navigate = useNavigate();
  const { supplier } = location.state || {};
  const username = localStorage.getItem("username");

  const [formData, setFormData] = useState({
    name: supplier?.name || "",
    email: supplier?.email || "",
    mobile: supplier?.mobile || "",
    gender: supplier?.gender || "",
    country: supplier?.country || "",
    state: supplier?.state || "",
    city: supplier?.city || "",
    address: supplier?.address || "",
    createdBy: username,
  });

  const [errors, setErrors] = useState({});
  const [successMessage, setSuccessMessage] = useState("");
  const [countries, setCountries] = useState([]);
  const [states, setStates] = useState([]);
  const [cities, setCities] = useState([]);

  useEffect(() => {
    axios
      .get("https://countriesnow.space/api/v0.1/countries")
      .then((res) => setCountries(res.data.data))
      .catch((err) => console.error(err));
  }, []);

  useEffect(() => {
    if (!formData.country) {
      setStates([]);
      setFormData((prev) => ({ ...prev, state: "", city: "" }));
      return;
    }
    axios
      .post("https://countriesnow.space/api/v0.1/countries/states", {
        country: formData.country,
      })
      .then((res) => setStates(res.data.data.states || []))
      .catch((err) => console.error(err));
  }, [formData.country]);

  useEffect(() => {
    if (!formData.state) {
      setCities([]);
      setFormData((prev) => ({ ...prev, city: "" }));
      return;
    }
    axios
      .post("https://countriesnow.space/api/v0.1/countries/state/cities", {
        country: formData.country,
        state: formData.state,
      })
      .then((res) => setCities(res.data.data || []))
      .catch((err) => console.error(err));
  }, [formData.state]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: name === "mobile" ? value.replace(/\D/g, "") : value,
    });
  };

  const handleSelectChange = (e) => {
    const { name, value } = e.target;
    if (name === "country") {
      setFormData((prev) => ({
        ...prev,
        country: value,
        state: "",
        city: "",
      }));
    } else if (name === "state") {
      setFormData((prev) => ({
        ...prev,
        state: value,
        city: "",
      }));
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  const validate = () => {
    const newErrors = {};
    const nameRegex = /^[A-Za-z\s]+$/;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const mobileRegex = /^\d{10}$/;

    if (!formData.name.trim()) newErrors.name = "Name is required";
    else if (!nameRegex.test(formData.name)) newErrors.name = "Invalid name";

    if (!formData.email.trim()) newErrors.email = "Email is required";
    else if (!emailRegex.test(formData.email)) newErrors.email = "Invalid email";

    if (!formData.mobile.trim()) newErrors.mobile = "Mobile is required";
    else if (!mobileRegex.test(formData.mobile)) newErrors.mobile = "Must be 10 digits";

    if (!formData.gender) newErrors.gender = "Select gender";

    ["country", "state", "city"].forEach((field) => {
      if (!formData[field].trim()) newErrors[field] = `${field} is required`;
    });

    if (!formData.address.trim()) newErrors.address = "Address is required";

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSuccessMessage("");
    if (!validate()) return;

    const updatedSupplier = { id: supplier.id, ...formData };

const updatedDataForComparison = { ...formData };
delete updatedDataForComparison.createdBy;

const isUnchanged = Object.keys(updatedDataForComparison).every(
  (key) => updatedDataForComparison[key] === supplier[key]
);

    // const isUnchanged = Object.keys(formData).every(
    //   (key) => formData[key] === supplier[key]
    // );

    if (isUnchanged) {
      setErrors({ form: "No changes made!" });
      setTimeout(() => {
        setErrors({});
      }, 1500);
      return;
    }

    try {
      await api.put(
        `/suppliers/${supplier.id}`,
        updatedSupplier
      );
      setSuccessMessage("Supplier updated successfully!");
      setErrors({});
      setTimeout(() => navigate("/view/supplier"), 1500);
    } catch (error) {
      console.error("Error updating supplier:", error);
      setErrors({ form: "Failed to update supplier. Please try again." });
    }
  };

  const handleClear = () => {
    const confirmClear = window.confirm("Are you sure you want to clear the form? This will revert all changes to the original values.");
    if (confirmClear) {
     setFormData({
      name: "",
      email: "",
      mobile: "",
      gender: "",
      country: "",
      state: "",
      city: "",
      address: "",
      createdBy: "Admin",
    });
    setErrors({});
    }
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-12 col-lg-12">
          <h2 className="text-center mb-4">Update Supplier</h2>

          {errors.form && <div className="alert alert-danger">{errors.form}</div>}
          {successMessage && (
            <div className="alert alert-success">{successMessage}</div>
          )}

          <form
            onSubmit={handleSubmit}
            className="border rounded p-4 shadow-sm bg-white"
          >
            <div className="mb-3">
              <label className="form-label fw-semibold">Supplier ID:</label>
              <input
                type="text"
                className="form-control"
                value={supplier.id}
                disabled
              />
            </div>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label">Name:</label>
                <input
                  type="text"
                  className={`form-control ${errors.name ? "is-invalid" : ""}`}
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  placeholder="Enter Supplier Name"
                />
                {errors.name && <div className="invalid-feedback">{errors.name}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Email:</label>
                <input
                  type="email"
                  className={`form-control ${errors.email ? "is-invalid" : ""}`}
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="Enter Supplier Email"
                />
                {errors.email && <div className="invalid-feedback">{errors.email}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Mobile:</label>
                <input
                  type="tel"
                  maxLength="10"
                  className={`form-control ${errors.mobile ? "is-invalid" : ""}`}
                  name="mobile"
                  value={formData.mobile}
                  onChange={handleChange}
                  placeholder="Enter Mobile Number"
                />
                {errors.mobile && <div className="invalid-feedback">{errors.mobile}</div>}
              </div>
              <div className="col-md-6">
                <label className="form-label">Gender:</label>
                <select
                  name="gender"
                  className={`form-select ${errors.gender ? "is-invalid" : ""}`}
                  value={formData.gender}
                  onChange={handleChange}
                >
                  <option value="">Select Gender</option>
                  <option value="M">Male</option>
                  <option value="F">Female</option>
                </select>
                {errors.gender && <div className="invalid-feedback">{errors.gender}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">Country:</label>
                <select
                  name="country"
                  className={`form-select ${errors.country ? "is-invalid" : ""}`}
                  value={formData.country}
                  onChange={handleSelectChange}
                >
                  <option value="">Select Country</option>
                  {countries.map((c) => (
                    <option key={c.country} value={c.country}>
                      {c.country}
                    </option>
                  ))}
                </select>
                {errors.country && <div className="invalid-feedback">{errors.country}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">State:</label>
                <select
                  name="state"
                  className={`form-select ${errors.state ? "is-invalid" : ""}`}
                  value={formData.state}
                  onChange={handleSelectChange}
                  disabled={!formData.country}
                >
                  <option value="">Select State</option>
                  {states.map((s) => (
                    <option key={s.name || s} value={s.name || s}>
                      {s.name || s}
                    </option>
                  ))}
                </select>
                {errors.state && <div className="invalid-feedback">{errors.state}</div>}
              </div>
              <div className="col-md-4">
                <label className="form-label">City:</label>
                <select
                  name="city"
                  className={`form-select ${errors.city ? "is-invalid" : ""}`}
                  value={formData.city}
                  onChange={handleSelectChange}
                  disabled={!formData.state}
                >
                  <option value="">Select City</option>
                  {cities.map((c) => (
                    <option key={c} value={c}>
                      {c}
                    </option>
                  ))}
                </select>
                {errors.city && <div className="invalid-feedback">{errors.city}</div>}
              </div>
              <div className="col-12">
                <label className="form-label">Address:</label>
                <textarea
                  name="address"
                  className={`form-control ${errors.address ? "is-invalid" : ""}`}
                  value={formData.address}
                  onChange={handleChange}
                  placeholder="Enter Address"
                />
                {errors.address && <div className="invalid-feedback">{errors.address}</div>}
              </div>
            </div>

            <div className="d-flex justify-content-center gap-3 mt-4">
              <button type="submit" className="btn btn-success px-5">
                Update
              </button>
              <button
                type="button"
                className="btn btn-danger mx-3"
                onClick={handleClear}
              >
                Clear
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
