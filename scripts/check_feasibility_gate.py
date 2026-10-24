#!/usr/bin/env python3
"""Validate the gate manifest. A passing script is NEVER an identity/session test."""
import argparse
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument("--require-go", action="store_true")
args = parser.parse_args()
gate = json.loads((root / "docs/feasibility-gate.json").read_text())


def require(condition, message):
    if not condition:
        raise SystemExit(message)


require(gate["status"] in {"GO", "CONDITIONAL GO", "NO-GO", "UNVERIFIED"}, "Status gate tidak valid")
require(isinstance(gate["evidence"], list), "Daftar evidence diperlukan")
if "product_scope" in gate:
    require(gate["product_scope"] == "MANUAL_FLOW_SWITCHING", "Scope produk tidak dikenali")
    approval = gate.get("scope_approval", {})
    require(approval.get("status") == "USER_APPROVED", "Perubahan scope memerlukan persetujuan pengguna")
    require(bool(approval.get("user_quote")), "Catatan keputusan pengguna diperlukan")
    scope_doc = (root / approval.get("document", "")).resolve()
    require(scope_doc.is_relative_to(root) and scope_doc.is_file(), "Dokumen scope tidak tersedia")
if gate["status"] == "GO":
    require(gate["evidence"], "GO memerlukan bukti perangkat yang ditinjau")
    for evidence in gate["evidence"]:
        require(isinstance(evidence, str), "Path bukti harus berupa string")
        path = (root / evidence).resolve()
        require(path.is_relative_to(root), "Bukti harus berada di repository")
        require(path.is_file() and path.stat().st_size > 0, f"Bukti tidak tersedia: {evidence}")
if args.require_go:
    require(gate["status"] == "GO", f"Rilis diblokir: feasibility gate {gate['status']}")
print(f"Gate manifest valid: {gate['status']}. Ini bukan pengujian isolasi sesi.")
if "product_scope" in gate:
    print("Organizer manual disetujui pengguna; isolasi otomatis dan produksi tetap tidak dinyatakan GO.")
