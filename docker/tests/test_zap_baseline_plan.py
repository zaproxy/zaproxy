import importlib.util
import os
import sys
import tempfile
import unittest
from pathlib import Path
from unittest.mock import Mock, patch

import yaml


class TestZapBaselinePlan(unittest.TestCase):
    target = "https://example.com/"
    config_content = "10001\tFAIL\tignore\tCustom message\n"

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

    def generate_plan(self, args, config_content=None, url_content=None):
        zap_baseline = self.load_module()

        with tempfile.TemporaryDirectory() as home_dir:
            summary_file = os.path.join(home_dir, "zap_out.json")
            plan_path = os.path.join(home_dir, "zap.yaml")

            if config_content is not None:
                config_path = os.path.join(home_dir, "config.conf")
                Path(config_path).write_text(config_content, encoding="utf-8")

            original_cwd = os.getcwd()
            os.chdir(home_dir)
            try:
                with patch.dict(os.environ, {"HOME": home_dir}, clear=True):
                    with patch.object(zap_baseline, "check_zap_client_version"):
                        with patch.object(zap_baseline, "running_in_docker", return_value=False):
                            with patch.object(zap_baseline.Path, "home", return_value=Path(home_dir)):
                                urlopen_patch = patch.object(
                                    zap_baseline,
                                    "urlopen",
                                    return_value=Mock(read=lambda: url_content.encode("utf-8"))
                                )
                                if url_content is not None:
                                    with urlopen_patch:
                                        with self.assertRaises(SystemExit) as exc:
                                            zap_baseline.main(args)
                                else:
                                    with self.assertRaises(SystemExit) as exc:
                                        zap_baseline.main(args)
                                self.assertEqual(0, exc.exception.code)
            finally:
                os.chdir(original_cwd)

            self.assertTrue(os.path.exists(plan_path))
            generated_plan = yaml.safe_load(Path(plan_path).read_text(encoding="utf-8"))
            return generated_plan, summary_file

    def assert_plan_matches_fixture(self, args, fixture_name, config_content=None, url_content=None):
        generated_plan, summary_file = self.generate_plan(
            args,
            config_content=config_content,
            url_content=url_content,
        )
        expected_plan = self.load_fixture_plan(fixture_name, summary_file)
        self.assertEqual(expected_plan, generated_plan)

    def test_param_plan_only(self):
        args = ["--plan-only", "-t", self.target]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_lc_plan-only.yaml")

    def test_param_c(self):
        args = ["--plan-only", "-t", self.target, "-c", "config.conf"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_lc_c.yaml", config_content=self.config_content)

    def test_param_u(self):
        args = ["--plan-only", "-t", self.target, "-u", "https://config.example.com/rules.conf"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_lc_u.yaml", url_content=self.config_content)

    def test_param_m(self):
        args = ["--plan-only", "-t", self.target, "-m", "5"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_lc_m.yaml")

    def test_param_r(self):
        args = ["--plan-only", "-t", self.target, "-r", "report.html"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_lc_r.yaml")

    def test_param_w(self):
        args = ["--plan-only", "-t", self.target, "-w", "report.md"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_lc_w.yaml")

    def test_param_x(self):
        args = ["--plan-only", "-t", self.target, "-x", "report.xml"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_lc_x.yaml")

    def test_param_J(self):
        args = ["--plan-only", "-t", self.target, "-J", "report.json"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_uc_j.yaml")

    def test_param_a(self):
        args = ["--plan-only", "-t", self.target, "-a"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_lc_a.yaml")

    def test_param_d(self):
        args = ["--plan-only", "-t", self.target, "-d"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_lc_d.yaml")

    def test_param_P(self):
        args = ["--plan-only", "-t", self.target, "-P", "12345"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_uc_p.yaml")

    def test_param_I(self):
        args = ["--plan-only", "-t", self.target, "-I"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_uc_i.yaml")

    def test_param_j(self):
        args = ["--plan-only", "-t", self.target, "-j"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_lc_j.yaml")

    def test_param_s(self):
        args = ["--plan-only", "-t", self.target, "-s"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_lc_s.yaml")

    def test_param_T(self):
        args = ["--plan-only", "-t", self.target, "-T", "10"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_uc_t.yaml")

    def test_param_D(self):
        args = ["--plan-only", "-t", self.target, "-D", "5"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_uc_d.yaml")

    def test_param_z(self):
        args = ["--plan-only", "-t", self.target, "-z", "-config aaa=bbb"]
        self.assert_plan_matches_fixture(args, "baseline_plan_param_lc_z.yaml")

    def test_plan_only_unsupported_option(self):
        zap_baseline = self.load_module()
        args = ["--plan-only", "-t", self.target, "-n", "context.context"]

        with tempfile.TemporaryDirectory() as home_dir:
            plan_path = os.path.join(home_dir, "zap.yaml")
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

            self.assertTrue(any("-n" in message for message in log_capture.output))
            self.assertFalse(os.path.exists(plan_path))

    def test_plan_only_requires_mounted_workdir_in_docker(self):
        zap_baseline = self.load_module()
        args = ["--plan-only", "-t", self.target]

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
