import importlib.util
import os
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import yaml


class TestZapBaselinePlan(unittest.TestCase):
    def load_module(self):
        docker_dir = Path(__file__).resolve().parents[1]
        module_path = docker_dir / "zap-baseline.py"
        module_name = "zap_baseline_test"

        if module_name in sys.modules:
            del sys.modules[module_name]

        spec = importlib.util.spec_from_file_location(module_name, module_path)
        module = importlib.util.module_from_spec(spec)
        sys.modules[module_name] = module
        spec.loader.exec_module(module)
        return module

    def load_fixture_plan(self, fixture_name, summary_file):
        fixtures_dir = Path(__file__).resolve().parent / "fixtures"
        fixture_path = fixtures_dir / fixture_name
        raw = fixture_path.read_text(encoding="utf-8")
        raw = raw.replace("{SUMMARY_FILE}", summary_file)
        return yaml.safe_load(raw)

    def test_plan_only_supported_options(self):
        zap_baseline = self.load_module()
        target = "https://example.com/path"

        with tempfile.TemporaryDirectory() as home_dir:
            summary_file = os.path.join(home_dir, "zap_out.json")
            plan_path = os.path.join(home_dir, "zap.yaml")
            args = [
                "--plan-only",
                "-t", target,
                "-m", "5",
                "-j",
                "-T", "10",
                "-s"
            ]

            original_cwd = os.getcwd()
            os.chdir(home_dir)
            try:
                with patch.dict(os.environ, {"HOME": home_dir}, clear=True):
                    with patch.object(zap_baseline, "check_zap_client_version"):
                        with patch.object(zap_baseline, "running_in_docker", return_value=False):
                            with patch.object(zap_baseline.Path, "home", return_value=Path(home_dir)):
                                with self.assertRaises(SystemExit) as exc:
                                    zap_baseline.main(args)
                                self.assertEqual(0, exc.exception.code)
            finally:
                os.chdir(original_cwd)

            self.assertTrue(os.path.exists(plan_path))
            generated_plan = yaml.safe_load(Path(plan_path).read_text(encoding="utf-8"))
            expected_plan = self.load_fixture_plan("baseline_plan_supported.yaml", summary_file)
            self.assertEqual(expected_plan, generated_plan)

    def test_plan_only_unsupported_option(self):
        zap_baseline = self.load_module()
        target = "https://example.com/"

        with tempfile.TemporaryDirectory() as home_dir:
            plan_path = os.path.join(home_dir, "zap.yaml")

            args = [
                "--plan-only",
                "-t", target,
                "-D", "5"
            ]

            original_cwd = os.getcwd()
            os.chdir(home_dir)
            try:
                with patch.dict(os.environ, {"HOME": home_dir}, clear=True):
                    with patch.object(zap_baseline, "check_zap_client_version"):
                        with patch.object(zap_baseline, "running_in_docker", return_value=False):
                            with patch.object(zap_baseline.Path, "home", return_value=Path(home_dir)):
                                with self.assertLogs(level="WARNING") as log_capture:
                                    with self.assertRaises(SystemExit) as exc:
                                        zap_baseline.main(args)
                                self.assertEqual(3, exc.exception.code)
            finally:
                os.chdir(original_cwd)

            self.assertTrue(any("-D" in message for message in log_capture.output))
            self.assertFalse(os.path.exists(plan_path))

    def test_plan_only_requires_mounted_workdir_in_docker(self):
        zap_baseline = self.load_module()
        target = "https://example.com/"
        args = [
            "--plan-only",
            "-t", target,
        ]

        real_exists = os.path.exists

        def exists_side_effect(path):
            if path == "/zap/wrk/":
                return False
            return real_exists(path)

        with patch.dict(os.environ, {"IS_CONTAINERIZED": "true"}):
            with patch("os.path.exists", side_effect=exists_side_effect):
                with self.assertLogs(level="WARNING") as log_capture:
                    with self.assertRaises(SystemExit) as exc:
                        zap_baseline.main(args)
                self.assertEqual(3, exc.exception.code)

        self.assertTrue(any("/zap/wrk" in message for message in log_capture.output))
